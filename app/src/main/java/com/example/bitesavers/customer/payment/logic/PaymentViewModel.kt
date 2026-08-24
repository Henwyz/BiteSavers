package com.example.bitesavers.customer.payment.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.payment.data.PaymentMethodsUiState
import com.example.bitesavers.customer.payment.data.SavedBankCard
import com.example.bitesavers.customer.payment.ui.PaymentMethodsUiEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class PaymentMethodsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    fun onEvent(event: PaymentMethodsUiEvent) {
        when (event) {
            // Card Form Input Handling
            is PaymentMethodsUiEvent.OnToggleAddCard -> {
                _uiState.update { it.copy(isAddingCard = !it.isAddingCard) }
            }
            is PaymentMethodsUiEvent.OnCardNumberChange -> {
                val digitsOnly = event.number.filter { it.isDigit() }
                if (digitsOnly.length <= 16) {
                    _uiState.update { it.copy(cardNumber = digitsOnly) }
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
                val digitsOnly = event.cvv.filter { it.isDigit() }
                if (digitsOnly.length <= 3) {
                    _uiState.update { it.copy(cvv = digitsOnly) }
                }
            }
            is PaymentMethodsUiEvent.OnSaveCard -> {
                saveCard()
            }

            // Card Management
            is PaymentMethodsUiEvent.OnDeleteCard -> {
                _uiState.update { current ->
                    val filtered = current.savedCards.filterNot { it.id == event.cardId }
                    // If we deleted the default card, set the first remaining card as default
                    val updatedList = if (filtered.none { it.isDefault } && filtered.isNotEmpty()) {
                        filtered.mapIndexed { index, card ->
                            if (index == 0) card.copy(isDefault = true) else card
                        }
                    } else {
                        filtered
                    }
                    current.copy(savedCards = updatedList)
                }
            }
            is PaymentMethodsUiEvent.OnSetDefaultCard -> {
                _uiState.update { current ->
                    current.copy(
                        savedCards = current.savedCards.map { card ->
                            card.copy(isDefault = card.id == event.cardId)
                        }
                    )
                }
            }

            // E-Wallet Link Toggle
            is PaymentMethodsUiEvent.OnToggleTngLink -> {
                _uiState.update { it.copy(isTngLinked = !it.isTngLinked) }
            }

            // Top Up Balance
            is PaymentMethodsUiEvent.OnShowTopUpSheet -> {
                _uiState.update { it.copy(isTopUpSheetVisible = true) }
            }
            is PaymentMethodsUiEvent.OnDismissTopUpSheet -> {
                _uiState.update { it.copy(isTopUpSheetVisible = false) }
            }
            is PaymentMethodsUiEvent.OnConfirmTopUp -> {
                _uiState.update {
                    it.copy(
                        walletBalance = it.walletBalance + event.amount,
                        isTopUpSheetVisible = false
                    )
                }
            }

            is PaymentMethodsUiEvent.OnNavigateBack -> {
                // Handled in Navigation
            }
        }
    }

    private fun saveCard() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val newCard = SavedBankCard(
                id = UUID.randomUUID().toString(),
                cardHolder = currentState.cardHolder.trim(),
                lastFourDigits = currentState.cardNumber.takeLast(4),
                expiryDate = currentState.expiryDate.trim(),
                isDefault = currentState.savedCards.isEmpty()
            )

            _uiState.update {
                it.copy(
                    savedCards = it.savedCards + newCard,
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