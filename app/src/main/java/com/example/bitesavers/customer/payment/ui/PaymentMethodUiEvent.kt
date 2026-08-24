package com.example.bitesavers.customer.payment.ui

sealed interface PaymentMethodsUiEvent {
    object OnToggleAddCard : PaymentMethodsUiEvent
    data class OnCardNumberChange(val number: String) : PaymentMethodsUiEvent
    data class OnCardHolderChange(val name: String) : PaymentMethodsUiEvent
    data class OnExpiryDateChange(val expiry: String) : PaymentMethodsUiEvent
    data class OnCvvChange(val cvv: String) : PaymentMethodsUiEvent
    object OnSaveCard : PaymentMethodsUiEvent
    object OnNavigateBack : PaymentMethodsUiEvent
}