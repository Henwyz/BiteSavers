package com.example.bitesavers.customer.details.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.details.data.FoodDetailUiState
import com.example.bitesavers.customer.details.ui.FoodDetailUiEvent
import com.example.bitesavers.data.repository.OfferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FoodDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val repository: OfferRepository = OfferRepository()

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()

    init {
        val offerId: String? = savedStateHandle.get<String>("offerId")
        if (offerId != null) {
            fetchOfferDetails(offerId)
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Offer not found") }
        }
    }

    // THE SWITCHBOARD
    fun onEvent(event: FoodDetailUiEvent) {
        when (event) {
            is FoodDetailUiEvent.OnIncreaseQuantity -> increaseQuantity()
            is FoodDetailUiEvent.OnDecreaseQuantity -> decreaseQuantity()
            is FoodDetailUiEvent.OnNavigateBack -> {
                // Any ViewModel cleanup before leaving
            }
            is FoodDetailUiEvent.OnReserveClicked -> {
                // send the card data to the database
            }
        }
    }

    private fun fetchOfferDetails(offerId: String) {
        // Run database fetch on a background coroutine thread
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Fetch the real item from Supabase
            val offer = repository.fetchOfferById(offerId)

            if (offer != null) {
                // Preserved your custom temperature and safety logic
                val temp = offer.liveTemperature
                val isSafe = when (offer.storageType.uppercase()) {
                    "HOT" -> temp >= 60.0
                    "COLD" -> temp in 2.0..8.0
                    else -> true
                }
                val tempString = "Live temp: ${temp}°C - within safe ${offer.storageType.lowercase()} storage zone"

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        offer = offer,
                        quantity = 1,
                        totalPrice = offer.currentPrice,
                        temperatureText = tempString,
                        isTemperatureSafe = isSafe
                    )
                }
            } else {
                _uiState.update { current ->
                    current.copy(isLoading = false, errorMessage = "Offer not found")
                }
            }
        }
    }

    // Notice these are private now! The UI can only access them via the switchboard.
    private fun increaseQuantity() {
        _uiState.update { current ->
            val maxStock = current.offer?.quantityLeft ?: 1
            if (current.quantity < maxStock) {
                val newQuantity = current.quantity + 1
                val newTotal = (current.offer?.currentPrice ?: 0.0) * newQuantity
                current.copy(quantity = newQuantity, totalPrice = newTotal)
            } else {
                current
            }
        }
    }

    private fun decreaseQuantity() {
        _uiState.update { current ->
            if (current.quantity > 1) {
                val newQuantity = current.quantity - 1
                val newTotal = (current.offer?.currentPrice ?: 0.0) * newQuantity
                current.copy(quantity = newQuantity, totalPrice = newTotal)
            } else {
                current
            }
        }
    }
}