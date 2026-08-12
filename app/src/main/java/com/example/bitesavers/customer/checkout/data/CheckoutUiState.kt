package com.example.bitesavers.customer.checkout.data

data class CheckoutUiState(
    val storeName: String = "",
    val itemName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val walletBalance: Double = 0.0,
    val isPaymentSuccessful: Boolean = false // True when checkout is done!
) {
    // Automatically calculates the total based on the current state
    val totalPrice: Double
        get() = quantity * unitPrice
}