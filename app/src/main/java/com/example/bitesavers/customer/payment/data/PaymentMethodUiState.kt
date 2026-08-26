package com.example.bitesavers.customer.payment.data

data class SavedBankCard(
    val id: String = "",
    val cardHolder: String = "",
    val lastFourDigits: String = "",
    val expiryDate: String = "",
    val isDefault: Boolean = false
)

data class PaymentMethodsUiState(
    val walletBalance: Double = 67.50,
    val savedCards: List<SavedBankCard> = emptyList(),
    val isTngLinked: Boolean = false,
    val tngPhone: String = "",

    // Add Card Form
    val isAddingCard: Boolean = false,
    val cardNumber: String = "",
    val cardHolder: String = "",
    val expiryDate: String = "",
    val cvv: String = "",

    // Top-Up State
    val isTopUpSheetVisible: Boolean = false,
    val isProcessingPayment: Boolean = false,

    // Link E-Wallet (Phone + OTP) State
    val isLinkWalletSheetVisible: Boolean = false,
    val linkWalletPhone: String = "",
    val linkWalletOtp: String = "",
    val isOtpStep: Boolean = false,

    val isLoading: Boolean = false,
    val isSavedSuccess: Boolean = false
) {
    val isFormValid: Boolean
        get() = cardNumber.length == 16 &&
                cardHolder.isNotBlank() &&
                expiryDate.length == 5 &&
                cvv.length == 3
}