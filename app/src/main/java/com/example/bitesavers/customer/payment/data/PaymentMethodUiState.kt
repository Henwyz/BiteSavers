package com.example.bitesavers.customer.payment.data

data class PaymentMethodsUiState(
    // Wallet State
    val walletBalance: Double = 67.50,
    val isTopUpSheetVisible: Boolean = false,

    // E-Wallet State
    val isTngLinked: Boolean = true,
    val tngPhone: String = "+60 12-*** 7890",

    // Saved Bank Cards
    val savedCards: List<SavedBankCard> = listOf(
        SavedBankCard(
            id = "card_default_1",
            cardHolder = "Michelle Lim",
            lastFourDigits = "4321",
            expiryDate = "08/28",
            isDefault = true
        )
    ),

    // Add Card Form State
    val isAddingCard: Boolean = false,
    val cardNumber: String = "",
    val cardHolder: String = "",
    val expiryDate: String = "",
    val cvv: String = "",
    val isLoading: Boolean = false,
    val isSavedSuccess: Boolean = false
) {
    val isFormValid: Boolean
        get() = cardNumber.length == 16 &&
                cardHolder.isNotBlank() &&
                expiryDate.isNotBlank() &&
                cvv.length == 3
}

data class SavedBankCard(
    val id: String,
    val cardHolder: String,
    val lastFourDigits: String,
    val expiryDate: String,
    val isDefault: Boolean = false
)