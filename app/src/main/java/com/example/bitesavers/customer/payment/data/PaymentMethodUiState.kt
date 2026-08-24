package com.example.bitesavers.customer.payment.data

import com.example.bitesavers.data.model.PaymentMethod

data class PaymentMethodsUiState(
    val walletBalance: Double = 67.50,
    val savedMethods: List<PaymentMethod> = listOf(
        PaymentMethod.BITESAVER_PAY,
        PaymentMethod.TNG_EWALLET,
        PaymentMethod.CASH_ON_PICKUP
    ),
    val isAddingCard: Boolean = false,
    val cardNumber: String = "",
    val cardHolder: String = "",
    val expiryDate: String = "",
    val cvv: String = "",
    val isLoading: Boolean = false,
    val isSavedSuccess: Boolean = false
) {
    val isFormValid: Boolean
        get() = cardNumber.length >= 16 &&
                cardHolder.isNotBlank() &&
                expiryDate.isNotBlank() &&
                cvv.length == 3
}