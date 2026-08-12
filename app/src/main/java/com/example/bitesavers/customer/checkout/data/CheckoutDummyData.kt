package com.example.bitesavers.customer.checkout.data

object CheckoutDummyData {
    fun initialState() = CheckoutUiState(
        storeName = "Artisan Bakery",
        itemName = "Butter Croissant",
        quantity = 2,
        unitPrice = 2.50,
        walletBalance = 45.50,
        isPaymentSuccessful = false
    )
}