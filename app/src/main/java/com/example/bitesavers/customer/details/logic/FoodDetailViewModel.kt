package com.example.bitesavers.customer.details.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.details.data.FoodDetailUiState
import com.example.bitesavers.customer.details.ui.FoodDetailUiEvent
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.repository.OfferRepository
import com.example.bitesavers.data.repository.SavedRepository
import com.example.bitesavers.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FoodDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val repository: OfferRepository = OfferRepository()
    private val savedRepository: SavedRepository = SavedRepository()
    private val userRepository: UserRepository = UserRepository()

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()

    // Holds background polling and countdown coroutine jobs
    private var pollingJob: Job? = null
    private var countdownJob: Job? = null
    private var isNgoApprovedUser: Boolean = false

    init {
        val offerId: String? = savedStateHandle.get<String>("offerId")
        if (!offerId.isNullOrBlank()) {
            startPolling(offerId)
            observeBookmarkStatus(offerId)
            startMinuteTicker()
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Offer not found") }
        }
    }

    // Ticks every 30 seconds to keep remaining minutes accurate without full network refetches
    private fun startMinuteTicker() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000L) // 30-second interval
                _uiState.value.offer?.let { offer ->
                    val (mins, statusText) = calculateRemainingMinutesAndStatus(offer)
                    val isFreeNgo = isNgoApprovedUser && offer.isEligibleForNgoFree
                    _uiState.update {
                        it.copy(
                            minutesToClose = mins,
                            liveTimeStatus = if (isFreeNgo) "Free Claim" else statusText
                        )
                    }
                }
            }
        }
    }

    // Periodically polls Supabase so customer UI reflects live IoT temperature updates and latest NGO status
    fun startPolling(offerId: String, intervalMillis: Long = 3000L) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            fetchOfferDetails(offerId, isInitialLoad = true)

            while (isActive) {
                delay(intervalMillis)
                fetchOfferDetails(offerId, isInitialLoad = false)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        countdownJob?.cancel()
        pollingJob = null
        countdownJob = null
    }

    private fun observeBookmarkStatus(offerId: String) {
        viewModelScope.launch {
            SavedRepository.savedOfferIds.collectLatest { savedIds ->
                _uiState.update { it.copy(isSaved = savedIds.contains(offerId)) }
            }
        }
    }

    fun onEvent(event: FoodDetailUiEvent) {
        when (event) {
            is FoodDetailUiEvent.OnIncreaseQuantity -> increaseQuantity()
            is FoodDetailUiEvent.OnDecreaseQuantity -> decreaseQuantity()
            is FoodDetailUiEvent.OnToggleBookmark -> toggleBookmark()
            is FoodDetailUiEvent.OnNavigateBack -> stopPolling()
            is FoodDetailUiEvent.OnReserveClicked -> Unit
            is FoodDetailUiEvent.OnStoreClicked -> Unit
        }
    }

    private fun toggleBookmark() {
        val offerId = _uiState.value.offer?.id ?: return
        val currentUserId = UserSession.currentUserId.value
        if (currentUserId.isNotBlank()) {
            viewModelScope.launch {
                savedRepository.toggleSaveOffer(
                    userId = currentUserId,
                    offerId = offerId
                )
            }
        }
    }

    private suspend fun fetchOfferDetails(offerId: String, isInitialLoad: Boolean = false) {
        if (isInitialLoad) {
            _uiState.update { it.copy(isLoading = true) }
        }

        try {
            // 1. Pull latest NGO approval status directly from Supabase users table
            val userId = UserSession.getUserId().ifBlank { UserSession.currentUserId.value }
            if (userId.isNotBlank()) {
                try {
                    val status = userRepository.fetchUserNgoStatus(userId)
                    isNgoApprovedUser = status.equals("APPROVED", ignoreCase = true)
                } catch (_: Exception) {}
            }

            // 2. Fetch the latest live offer data from Supabase
            val offer = repository.fetchOfferById(offerId)

            if (offer != null) {
                val temp = offer.liveTemperature
                val isHot = offer.storageType.equals("HOT", ignoreCase = true) ||
                        offer.storageType.contains("Hot", ignoreCase = true)

                val isSafe = if (isHot) temp >= 55.0 else temp <= 8.0

                // If active user is an approved NGO and item is in free claim window, price becomes RM 0.00
                val isFreeForNgo = isNgoApprovedUser && offer.isEligibleForNgoFree
                val unitPrice = if (isFreeForNgo) 0.0 else offer.currentPrice

                val (minsLeft, statusText) = calculateRemainingMinutesAndStatus(offer)

                _uiState.update { current ->
                    val selectedQty = if (isInitialLoad) 1 else current.quantity.coerceAtMost(offer.quantityLeft.coerceAtLeast(1))
                    current.copy(
                        isLoading = false,
                        offer = offer,
                        quantity = selectedQty,
                        totalPrice = unitPrice * selectedQty,
                        isTemperatureSafe = isSafe,
                        isNgoApproved = isNgoApprovedUser,
                        minutesToClose = minsLeft,
                        liveTimeStatus = if (isFreeForNgo) "Free Claim" else statusText,
                        errorMessage = null
                    )
                }
            } else {
                if (isInitialLoad) {
                    _uiState.update { current ->
                        current.copy(isLoading = false, errorMessage = "Offer not found")
                    }
                }
            }
        } catch (e: Exception) {
            if (isInitialLoad) {
                _uiState.update { current ->
                    current.copy(isLoading = false, errorMessage = e.message ?: "Failed to load offer details")
                }
            }
        }
    }

    // Accurately calculates remaining hours and minutes against device clock
    private fun calculateRemainingMinutesAndStatus(offer: OfferUiModel): Pair<Long, String> {
        val windowStr = offer.pickupWindow // e.g., "Today, 8:00 PM - 9:30 PM" or "21:30:00"
        val now = Calendar.getInstance()

        // Extract end time string (e.g. "9:30 PM" or "21:30")
        val endTimeStr = if (windowStr.contains("-")) {
            windowStr.substringAfter("-").trim()
        } else {
            windowStr.trim()
        }

        val targetCal = Calendar.getInstance()
        var parsed = false

        val formats = listOf("h:mm a", "hh:mm a", "H:mm", "HH:mm:ss", "HH:mm")
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                val date = sdf.parse(endTimeStr)
                if (date != null) {
                    val parsedCal = Calendar.getInstance().apply { time = date }
                    targetCal.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
                    targetCal.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
                    targetCal.set(Calendar.SECOND, 0)
                    parsed = true
                    break
                }
            } catch (_: Exception) {}
        }

        if (!parsed) {
            // Fallback to hoursToClose calculation if string cannot be parsed
            val mins = (offer.hoursToClose * 60).toLong()
            return if (mins > 0) mins to "Closes in ${offer.hoursToClose}h" else 0L to "Closed"
        }

        val diffMillis = targetCal.timeInMillis - now.timeInMillis
        val diffMinutes = diffMillis / (1000 * 60)

        return if (diffMinutes <= 0) {
            0L to "Closed"
        } else {
            val hours = diffMinutes / 60
            val minutes = diffMinutes % 60
            val label = when {
                hours > 0 && minutes > 0 -> "Closes in ${hours}h ${minutes}m"
                hours > 0 -> "Closes in ${hours}h"
                else -> "Closes in ${minutes}m"
            }
            diffMinutes to label
        }
    }

    private fun increaseQuantity() {
        _uiState.update { current ->
            val maxStock = current.offer?.quantityLeft ?: 1
            if (current.quantity < maxStock) {
                val newQuantity = current.quantity + 1
                val isFree = isNgoApprovedUser && (current.offer?.isEligibleForNgoFree == true)
                val unitPrice = if (isFree) 0.0 else (current.offer?.currentPrice ?: 0.0)
                current.copy(quantity = newQuantity, totalPrice = unitPrice * newQuantity)
            } else {
                current
            }
        }
    }

    private fun decreaseQuantity() {
        _uiState.update { current ->
            if (current.quantity > 1) {
                val newQuantity = current.quantity - 1
                val isFree = isNgoApprovedUser && (current.offer?.isEligibleForNgoFree == true)
                val unitPrice = if (isFree) 0.0 else (current.offer?.currentPrice ?: 0.0)
                current.copy(quantity = newQuantity, totalPrice = unitPrice * newQuantity)
            } else {
                current
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}