package com.example.bitesavers.customer.details.data

import com.example.bitesavers.data.model.OfferUiModel

data class FoodDetailUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val quantity: Int = 1,
    val totalPrice: Double = 0.0,
    val isTemperatureSafe: Boolean = true,
    val offer: OfferUiModel? = null,
    val isNgoApproved: Boolean = false,
    val liveTimeStatus: String = "",
    val minutesToClose: Long = 0L,
    val errorMessage: String? = null
) {
    val subtotal: Double
        get() = totalPrice

    val visualTaxAmount: Double
        get() = if (totalPrice > 0.0) Math.round(totalPrice * 0.06 * 100.0) / 100.0 else 0.0
}