package com.example.bitesavers.customer.checkout.ui

import com.example.bitesavers.data.model.PaymentMethod

sealed interface CheckoutUiEvent {
    data object OnNavigateBack : CheckoutUiEvent
    data object OnChangePaymentClicked : CheckoutUiEvent
    data object OnDismissPaymentSheet : CheckoutUiEvent
    data class OnSelectPaymentMethod(val method: PaymentMethod) : CheckoutUiEvent
    data object OnConfirmPaymentClicked : CheckoutUiEvent
    data object OnAddNewPaymentClicked : CheckoutUiEvent
}