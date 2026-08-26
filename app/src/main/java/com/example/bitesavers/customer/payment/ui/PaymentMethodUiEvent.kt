package com.example.bitesavers.customer.payment.ui

sealed interface PaymentMethodsUiEvent {
    // Card Form
    data object OnToggleAddCard : PaymentMethodsUiEvent
    data class OnCardNumberChange(val number: String) : PaymentMethodsUiEvent
    data class OnCardHolderChange(val name: String) : PaymentMethodsUiEvent
    data class OnExpiryDateChange(val expiry: String) : PaymentMethodsUiEvent
    data class OnCvvChange(val cvv: String) : PaymentMethodsUiEvent
    data object OnSaveCard : PaymentMethodsUiEvent

    // Card Management
    data class OnDeleteCard(val cardId: String) : PaymentMethodsUiEvent
    data class OnSetDefaultCard(val cardId: String) : PaymentMethodsUiEvent

    // E-Wallet Link Flow
    data object OnShowLinkWalletSheet : PaymentMethodsUiEvent
    data object OnDismissLinkWalletSheet : PaymentMethodsUiEvent
    data class OnLinkPhoneChange(val phone: String) : PaymentMethodsUiEvent
    data class OnLinkOtpChange(val otp: String) : PaymentMethodsUiEvent
    data object OnRequestOtp : PaymentMethodsUiEvent
    data object OnConfirmLinkWallet : PaymentMethodsUiEvent
    data object OnUnlinkWallet : PaymentMethodsUiEvent

    // Top-Up Flow
    data object OnShowTopUpSheet : PaymentMethodsUiEvent
    data object OnDismissTopUpSheet : PaymentMethodsUiEvent
    data class OnConfirmTopUp(val amount: Double, val paymentSource: String) : PaymentMethodsUiEvent

    // Navigation
    data object OnNavigateBack : PaymentMethodsUiEvent
}