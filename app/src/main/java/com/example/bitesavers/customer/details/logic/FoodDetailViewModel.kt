package com.example.bitesavers.customer.details.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.details.data.FoodDetailUiState
import com.example.bitesavers.customer.details.ui.FoodDetailUiEvent
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

class FoodDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val repository: OfferRepository = OfferRepository()
    private val savedRepository: SavedRepository = SavedRepository()
    private val userRepository: UserRepository = UserRepository()

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()

    // Holds background polling coroutine job
    private var pollingJob: Job? = null
    private var isNgoApprovedUser: Boolean = false

    init {
        val offerId: String? = savedStateHandle.get<String>("offerId")
        if (!offerId.isNullOrBlank()) {
            startPolling(offerId)
            observeBookmarkStatus(offerId)
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Offer not found") }
        }
    }

    // Periodically polls Supabase so customer UI reflects live IoT temperature updates and latest NGO status
    fun startPolling(offerId: String, intervalMillis: Long = 2000L) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            // Initial load displays loading indicator
            fetchOfferDetails(offerId, isInitialLoad = true)

            while (isActive) {
                delay(intervalMillis)
                // Subsequent polls update live telemetry quietly without re-triggering full page loader
                fetchOfferDetails(offerId, isInitialLoad = false)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun observeBookmarkStatus(offerId: String) {
        viewModelScope.launch {
            SavedRepository.savedOfferIds.collectLatest { savedIds ->
                _uiState.update { it.copy(isSaved = savedIds.contains(offerId)) }
            }
        }
    }

    // Handles incoming UI events from FoodDetailScreen
    fun onEvent(event: FoodDetailUiEvent) {
        when (event) {
            is FoodDetailUiEvent.OnIncreaseQuantity -> increaseQuantity()
            is FoodDetailUiEvent.OnDecreaseQuantity -> decreaseQuantity()
            is FoodDetailUiEvent.OnToggleBookmark -> toggleBookmark()
            is FoodDetailUiEvent.OnNavigateBack -> {
                stopPolling()
            }
            is FoodDetailUiEvent.OnReserveClicked -> {
                // Handled in the UI/Route layer for navigation
            }
            is FoodDetailUiEvent.OnStoreClicked -> {
                // Handled in the UI/Route layer for navigation
            }
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

                // Evaluates directional safe thresholds: Hot Box >= 55°C, Cold Chiller <= 8°C
                val isSafe = if (isHot) {
                    temp >= 55.0
                } else {
                    temp <= 8.0
                }

                // If active user is an approved NGO and item is in free claim window, price becomes RM 0.00
                val isFreeForNgo = isNgoApprovedUser && offer.isEligibleForNgoFree
                val unitPrice = if (isFreeForNgo) 0.0 else offer.currentPrice

                _uiState.update { current ->
                    val selectedQty = if (isInitialLoad) 1 else current.quantity.coerceAtMost(offer.quantityLeft.coerceAtLeast(1))
                    current.copy(
                        isLoading = false,
                        offer = offer,
                        quantity = selectedQty,
                        totalPrice = unitPrice * selectedQty,
                        isTemperatureSafe = isSafe,
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