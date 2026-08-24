package com.example.bitesavers.customer.payment.ui

sealed interface PaymentMethodsUiEvent {
    // Add / Edit Card Events
    data object OnToggleAddCard : PaymentMethodsUiEvent
    data class OnCardNumberChange(val number: String) : PaymentMethodsUiEvent
    data class OnCardHolderChange(val name: String) : PaymentMethodsUiEvent
    data class OnExpiryDateChange(val expiry: String) : PaymentMethodsUiEvent
    data class OnCvvChange(val cvv: String) : PaymentMethodsUiEvent
    data object OnSaveCard : PaymentMethodsUiEvent

    // Card Management Events
    data class OnDeleteCard(val cardId: String) : PaymentMethodsUiEvent
    data class OnSetDefaultCard(val cardId: String) : PaymentMethodsUiEvent

    // E-Wallet Link Events
    data object OnToggleTngLink : PaymentMethodsUiEvent

    // Top-Up Events
    data object OnShowTopUpSheet : PaymentMethodsUiEvent
    data object OnDismissTopUpSheet : PaymentMethodsUiEvent
    data class OnConfirmTopUp(val amount: Double) : PaymentMethodsUiEvent

    // Navigation
    data object OnNavigateBack : PaymentMethodsUiEvent
}