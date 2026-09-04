package com.example.bitesavers.customer.checkout.data

import com.example.bitesavers.data.model.PaymentMethod

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val storeName: String = "",
    val itemName: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 1,
    val walletBalance: Double = 0.0,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.BITESAVER_PAY,
    val isPaymentSheetVisible: Boolean = false,
    val isTngLinked: Boolean = false,
    val tngPhone: String = "",
    val savedCardDigits: String? = null,
    val errorResId: Int? = null,
    val placedOrderId: String? = null,
    val isPaymentSuccessful: Boolean = false
) {
    val totalPrice: Double
        get() = unitPrice * quantity

    val visualTaxAmount: Double
        get() = if (totalPrice > 0.0) Math.round(totalPrice * 0.06 * 100.0) / 100.0 else 0.0
}