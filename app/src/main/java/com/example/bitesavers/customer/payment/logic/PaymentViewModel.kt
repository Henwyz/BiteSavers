package com.example.bitesavers.customer.payment.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.payment.data.PaymentMethodsUiState
import com.example.bitesavers.customer.payment.ui.PaymentMethodsUiEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentMethodsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    fun onEvent(event: PaymentMethodsUiEvent) {
        when (event) {
            is PaymentMethodsUiEvent.OnToggleAddCard -> {
                _uiState.update { it.copy(isAddingCard = !it.isAddingCard) }
            }
            is PaymentMethodsUiEvent.OnCardNumberChange -> {
                if (event.number.length <= 16) {
                    _uiState.update { it.copy(cardNumber = event.number) }
                }
            }
            is PaymentMethodsUiEvent.OnCardHolderChange -> {
                _uiState.update { it.copy(cardHolder = event.name) }
            }
            is PaymentMethodsUiEvent.OnExpiryDateChange -> {
                if (event.expiry.length <= 5) {
                    _uiState.update { it.copy(expiryDate = event.expiry) }
                }
            }
            is PaymentMethodsUiEvent.OnCvvChange -> {
                if (event.cvv.length <= 3) {
                    _uiState.update { it.copy(cvv = event.cvv) }
                }
            }
            is PaymentMethodsUiEvent.OnSaveCard -> {
                saveCard()
            }
            is PaymentMethodsUiEvent.OnNavigateBack -> {
                // Handled in Navigation
            }
        }
    }

    private fun saveCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Simulating save
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAddingCard = false,
                    isSavedSuccess = true,
                    cardNumber = "",
                    cardHolder = "",
                    expiryDate = "",
                    cvv = ""
                )
            }
        }
    }
}