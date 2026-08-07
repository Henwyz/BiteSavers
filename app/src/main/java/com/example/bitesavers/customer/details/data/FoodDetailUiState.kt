package com.example.bitesavers.customer.details.data

import com.example.bitesavers.data.model.OfferUiModel

data class FoodDetailUiState(
    val isLoading: Boolean = true,
    val offer: OfferUiModel? = null,
    val quantity: Int = 1,
    val totalPrice: Double = 0.0,
    val errorMessage: String? = null,
    // NEW: Pass safety data from ViewModel to UI
    val temperatureText: String = "",
    val isTemperatureSafe: Boolean = true
)