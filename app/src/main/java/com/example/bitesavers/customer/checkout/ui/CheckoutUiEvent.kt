package com.example.bitesavers.customer.checkout.ui

import com.example.bitesavers.data.model.PaymentMethod

sealed interface CheckoutUiEvent {
    object OnNavigateBack : CheckoutUiEvent
    object OnChangePaymentClicked : CheckoutUiEvent
    object OnDismissPaymentSheet : CheckoutUiEvent
    data class OnSelectPaymentMethod(val method: PaymentMethod) : CheckoutUiEvent
    object OnConfirmPaymentClicked : CheckoutUiEvent

    object OnAddNewPaymentClicked : CheckoutUiEvent
}