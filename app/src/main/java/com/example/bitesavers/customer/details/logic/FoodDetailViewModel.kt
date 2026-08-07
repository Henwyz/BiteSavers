package com.example.bitesavers.customer.details.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.bitesavers.customer.details.data.FoodDetailUiState
import com.example.bitesavers.customer.discovery.data.DiscoveryDummyData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FoodDetailViewModel(
    savedStateHandle: SavedStateHandle // Automatically catches the ID from your AppNavHost!
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()

    init {
        // 1. Grab the "offerId" out of the navigation arguments
        val offerId: String? = savedStateHandle.get<String>("offerId")

        if (offerId != null) {
            fetchOfferDetails(offerId)
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Offer not found") }
        }
    }

    private fun fetchOfferDetails(offerId: String) {
        val offer = DiscoveryDummyData.defaultOffers.find { it.id == offerId }

        if (offer != null) {
            // Business Logic: Evaluate temperature safety
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

    fun onIncreaseQuantity() {
        _uiState.update { current ->
            // Prevent the user from ordering more than what is in stock!
            val maxStock = current.offer?.quantityLeft ?: 1

            if (current.quantity < maxStock) {
                val newQuantity = current.quantity + 1
                val newTotal = (current.offer?.currentPrice ?: 0.0) * newQuantity
                current.copy(quantity = newQuantity, totalPrice = newTotal)
            } else {
                current // Do nothing if they hit the max stock
            }
        }
    }

    fun onDecreaseQuantity() {
        _uiState.update { current ->
            // Prevent the user from ordering less than 1 item!
            if (current.quantity > 1) {
                val newQuantity = current.quantity - 1
                val newTotal = (current.offer?.currentPrice ?: 0.0) * newQuantity
                current.copy(quantity = newQuantity, totalPrice = newTotal)
            } else {
                current // Do nothing if they hit 1
            }
        }
    }
}