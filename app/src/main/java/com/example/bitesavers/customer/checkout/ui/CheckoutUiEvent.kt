package com.example.bitesavers.customer.checkout.ui

sealed interface CheckoutUiEvent {
    object OnNavigateBack : CheckoutUiEvent
    object OnChangePaymentClicked : CheckoutUiEvent
    object OnConfirmPaymentClicked : CheckoutUiEvent
}