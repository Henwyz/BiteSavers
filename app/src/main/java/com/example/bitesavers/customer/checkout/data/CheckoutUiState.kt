package com.example.bitesavers.customer.checkout.data

data class CheckoutUiState(
    val storeName: String = "",
    val itemName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val walletBalance: Double = 50.0, // Hardcoded initial wallet balance for testing
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPaymentSuccessful: Boolean = false,
    val placedOrderId: String? = null // 👈 ADD THIS FIELD HERE
) {
    val totalPrice: Double
        get() = quantity * unitPrice
}