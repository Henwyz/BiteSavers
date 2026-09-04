package com.example.bitesavers.customer.store.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.R
import com.example.bitesavers.customer.store.data.StoreDetailUiModel
import com.example.bitesavers.customer.store.data.StoreDetailUiState
import com.example.bitesavers.customer.store.ui.StoreDetailUiEvent
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.mapper.toUiModel
import com.example.bitesavers.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
                // Pulls the latest approved store information
                val storeDto = repository.getStoreById(storeId)
                if (storeDto != null) {
                    val openTime = storeDto.openingTime ?: "8:30 AM"
                    val closeTime = storeDto.closingTime ?: "9:00 PM"
                    val storeIdNonNull = storeDto.id.ifBlank { storeId }
                    val storeNameNonNull = storeDto.name.ifBlank { "Store" }
                    val storeRatingNonNull = storeDto.rating ?: 4.8

                    val storeUi = StoreDetailUiModel(
                        id = storeIdNonNull,
                        name = storeNameNonNull,
                        address = storeDto.address.ifBlank { "Location not specified" },
                        rating = storeRatingNonNull,
                        contactPhone = storeDto.contactPhone, // 👈 Now contains the latest approved phone number
                        operatingHours = "Mon – Sun: $openTime - $closeTime",
                        imageUrl = storeDto.imageUrl
                    )

                    val offerDtos = repository.getOffersByStoreId(storeId)
                    val offerUiList = offerDtos.map { dto ->
                        val discount = if (dto.originalPrice > 0) {
                            (((dto.originalPrice - dto.discountedPrice) / dto.originalPrice) * 100).toInt()
                        } else 0

                        OfferUiModel(
                            id = dto.id,
                            title = dto.title,
                            storeName = storeNameNonNull,
                            storeRating = storeRatingNonNull,
                            imageResId = R.drawable.ic_launcher_foreground,
                            imageUrl = dto.imageUrl,
                            discountPercent = discount,
                            currentPrice = dto.discountedPrice,
                            originalPrice = dto.originalPrice,
                            distanceKm = 0.0,
                            quantityLeft = dto.quantityAvailable,
                            hoursToClose = 2,
                            pickupWindow = "Today, $openTime - $closeTime",
                            category = DiscoveryCategory.BAKERY,
                            isEligibleForNgoFree = dto.isEligibleForNgoFree,
                            storageType = "HOT",
                            description = dto.description ?: "Fresh surplus food ready for rescue.",
                            latitude = storeDto.latitude ?: 5.4674,
                            longitude = storeDto.longitude ?: 100.2790
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
                        it.copy(isLoading = false, errorMessage = "Store not found")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "An unexpected error occurred")
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

    private fun toggleBookmark(offerId: String) {
        // Handle bookmark persistence if required
    }
}