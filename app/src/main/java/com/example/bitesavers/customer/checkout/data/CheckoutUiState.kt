package com.example.bitesavers.customer.checkout.data

import com.example.bitesavers.data.model.PaymentMethod

data class CheckoutUiState(
    val storeName: String = "",
    val itemName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val walletBalance: Double = 50.0, // Hardcoded initial wallet balance for testing
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPaymentSuccessful: Boolean = false,
    val placedOrderId: String? = null,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.BITESAVER_PAY,
    val isPaymentSheetVisible: Boolean = false
) {
    val totalPrice: Double
        get() = quantity * unitPrice
}