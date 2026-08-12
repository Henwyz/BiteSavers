package com.example.bitesavers.customer.checkout.logic

import androidx.lifecycle.ViewModel
import com.example.bitesavers.customer.checkout.data.CheckoutDummyData
import com.example.bitesavers.customer.checkout.data.CheckoutUiState
import com.example.bitesavers.customer.checkout.ui.CheckoutUiEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CheckoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutDummyData.initialState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun onEvent(event: CheckoutUiEvent) {
        when (event) {
            is CheckoutUiEvent.OnNavigateBack -> {
                // Handled in the Route wrapper
            }
            is CheckoutUiEvent.OnChangePaymentClicked -> {
                // TODO: Open a bottom sheet to change payment method
            }
            is CheckoutUiEvent.OnConfirmPaymentClicked -> {
                processPayment()
            }
        }
    }

    private fun processPayment() {
        _uiState.update { current ->
            // In a real app, you would call an API here.
            // For now, we simulate success by deducting the balance and flipping the success flag!
            current.copy(
                walletBalance = current.walletBalance - current.totalPrice,
                isPaymentSuccessful = true
            )
        }
    }
}