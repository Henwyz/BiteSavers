package com.example.bitesavers.customer.checkout.data

import androidx.annotation.StringRes
import com.example.bitesavers.data.model.PaymentMethod

data class CheckoutUiState(
    val storeName: String = "",
    val itemName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val walletBalance: Double = 0.0, // Live wallet balance
    val isLoading: Boolean = false,
    @StringRes val errorResId: Int? = null, // Using string resource ID instead of raw String
    val isPaymentSuccessful: Boolean = false,
    val placedOrderId: String? = null,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.BITESAVER_PAY,
    val isPaymentSheetVisible: Boolean = false
) {
    val totalPrice: Double
        get() = quantity * unitPrice
}