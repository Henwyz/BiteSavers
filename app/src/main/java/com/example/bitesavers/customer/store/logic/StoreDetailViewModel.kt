package com.example.bitesavers.customer.store.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.R
import com.example.bitesavers.customer.store.data.StoreDetailUiModel
import com.example.bitesavers.customer.store.data.StoreDetailUiState
import com.example.bitesavers.customer.store.ui.StoreDetailUiEvent
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Manages state for the restaurant detail screen including store info and active offers
class StoreDetailViewModel : ViewModel() {

    private val repository = StoreRepository()

    private val _uiState = MutableStateFlow(StoreDetailUiState())
    val uiState: StateFlow<StoreDetailUiState> = _uiState.asStateFlow()

    // Handles incoming UI events from StoreDetailScreen
    fun onEvent(event: StoreDetailUiEvent) {
        when (event) {
            is StoreDetailUiEvent.LoadStore -> loadStore(event.storeId)
            is StoreDetailUiEvent.OnToggleBookmark -> toggleBookmark(event.offerId)
            is StoreDetailUiEvent.OnOfferClicked,
            is StoreDetailUiEvent.OnCallClicked,
            is StoreDetailUiEvent.OnWhatsAppClicked,
            is StoreDetailUiEvent.OnBackClicked -> {
                // Navigation and intent actions are handled in the UI layer
            }
        }
    }

    // Loads store profile and its associated active offers from Supabase
    private fun loadStore(storeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val storeDto = repository.getStoreById(storeId)
                if (storeDto != null) {
                    val openFormatted = formatTimeDisplay(storeDto.openingTime) ?: "08:00 AM"
                    val closeFormatted = formatTimeDisplay(storeDto.closingTime) ?: "09:30 PM"

                    val storeUi = StoreDetailUiModel(
                        id = storeDto.id,
                        name = storeDto.name,
                        address = storeDto.address ?: "Location not specified",
                        rating = storeDto.rating,
                        contactPhone = storeDto.contactPhone,
                        operatingHours = "Mon – Sun: $openFormatted - $closeFormatted",
                        imageUrl = storeDto.imageUrl
                    )

                    val offerDtos = repository.getOffersByStoreId(storeId)
                    val offerUiList = offerDtos.map { dto ->
                        val discount = if (dto.originalPrice > 0) {
                            (((dto.originalPrice - dto.discountedPrice) / dto.originalPrice) * 100).toInt()
                        } else 0

                        val pickupStart = formatTimeDisplay(dto.pickupStart) ?: "08:00 PM"
                        val pickupEnd = formatTimeDisplay(dto.pickupEnd) ?: "10:00 PM"
                        val remainingHours = calculateHoursToClose(dto.pickupEnd)

                        val catEnum = runCatching {
                            DiscoveryCategory.valueOf(dto.category.uppercase())
                        }.getOrDefault(DiscoveryCategory.HOT_MEALS)

                        OfferUiModel(
                            id = dto.id,
                            storeId = storeDto.id,
                            title = dto.title,
                            storeName = storeDto.name,
                            storeRating = storeDto.rating,
                            imageResId = R.drawable.ic_launcher_foreground,
                            imageUrl = dto.imageUrl,
                            discountPercent = discount,
                            currentPrice = dto.discountedPrice,
                            originalPrice = dto.originalPrice,
                            distanceKm = 0.0,
                            quantityLeft = dto.quantityAvailable,
                            hoursToClose = remainingHours,
                            pickupWindow = "Today, $pickupStart - $pickupEnd",
                            category = catEnum,
                            isEligibleForNgoFree = dto.isEligibleForNgoFree,
                            storageType = "HOT",
                            description = dto.description ?: "Fresh surplus food ready for rescue.",
                            latitude = storeDto.latitude,
                            longitude = storeDto.longitude
                        )
                    }

                    _uiState.update {
                        it.copy(
                            store = storeUi,
                            offers = offerUiList,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Store not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }

    // Converts Supabase 24-hour SQL time string (e.g. 21:30:00 or 21:30) to user-friendly 12-hour AM/PM format
    private fun formatTimeDisplay(timeStr: String?): String? {
        if (timeStr.isNullOrBlank()) return null
        val patterns = arrayOf("HH:mm:ss", "HH:mm", "h:mm a")
        for (pattern in patterns) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.getDefault())
                parser.isLenient = false
                val date = parser.parse(timeStr.trim())
                if (date != null) {
                    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
                    return formatter.format(date)
                }
            } catch (_: Exception) {
                // Try next pattern candidate
            }
        }
        return timeStr
    }

    // Computes difference between current system time and pickup_end time window
    private fun calculateHoursToClose(pickupEndStr: String?): Int {
        if (pickupEndStr.isNullOrBlank()) return 2
        return try {
            val parser = SimpleDateFormat("HH:mm", Locale.getDefault())
            val targetDate = parser.parse(pickupEndStr.trim().take(5)) ?: return 2

            val now = Calendar.getInstance()
            val endCalendar = Calendar.getInstance().apply {
                time = targetDate
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            }

            val diffMillis = endCalendar.timeInMillis - now.timeInMillis
            val diffMinutes = diffMillis / (1000 * 60)

            if (diffMinutes <= 0) 0 else (diffMinutes / 60.0).toInt().coerceAtLeast(1)
        } catch (_: Exception) {
            2
        }
    }

    private fun toggleBookmark(offerId: String) {
        // Handle bookmark persistence if required
    }
}