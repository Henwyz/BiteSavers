package com.example.bitesavers.customer.payment.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.payment.data.PaymentMethodsUiState
import com.example.bitesavers.customer.payment.data.SavedBankCard
import com.example.bitesavers.customer.payment.ui.PaymentMethodsUiEvent
import com.example.bitesavers.data.repository.PaymentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentMethodsViewModel : ViewModel() {

    private val paymentRepository = PaymentRepository()

    private val _uiState = MutableStateFlow(PaymentMethodsUiState(isLoading = true))
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    // 1. Fetch live balance and saved methods from database
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val balance = paymentRepository.fetchWalletBalance()
            val methods = paymentRepository.fetchPaymentMethods()

            val tngMethod = methods.firstOrNull { it.type == "TNG_EWALLET" }
            val cards = methods
                .filter { it.type == "BANK_CARD" }
                .map { dto ->
                    SavedBankCard(
                        id = dto.id.orEmpty(),
                        cardHolder = dto.cardHolder.orEmpty(),
                        lastFourDigits = dto.lastFourDigits.orEmpty(),
                        expiryDate = dto.expiryDate.orEmpty(),
                        isDefault = dto.isDefault
                    )
                }

            _uiState.update { current ->
                current.copy(
                    walletBalance = balance,
                    savedCards = cards,
                    isTngLinked = tngMethod != null,
                    tngPhone = tngMethod?.linkedPhone.orEmpty(),
                    isLoading = false
                )
            }
        }
    }

    fun onEvent(event: PaymentMethodsUiEvent) {
        when (event) {
            // Card Input
            is PaymentMethodsUiEvent.OnToggleAddCard -> {
                _uiState.update {
                    it.copy(
                        isAddingCard = !it.isAddingCard,
                        cardNumber = "",
                        cardHolder = "",
                        expiryDate = "",
                        cvv = "",
                        isFormValid = false
                    )
                }
            }
            is PaymentMethodsUiEvent.OnCardNumberChange -> {
                val digitsOnly = event.number.filter { it.isDigit() }.take(16)
                _uiState.update {
                    val updated = it.copy(cardNumber = digitsOnly)
                    updated.copy(isFormValid = validateCardForm(updated))
                }
            }
            is PaymentMethodsUiEvent.OnCardHolderChange -> {
                _uiState.update {
                    val updated = it.copy(cardHolder = event.name)
                    updated.copy(isFormValid = validateCardForm(updated))
                }
            }
            is PaymentMethodsUiEvent.OnExpiryDateChange -> {
                // Keep raw digits so ExpiryDateVisualTransformation handles visual formatting without cursor jump
                val digitsOnly = event.expiry.filter { it.isDigit() }.take(4)
                _uiState.update {
                    val updated = it.copy(expiryDate = digitsOnly)
                    updated.copy(isFormValid = validateCardForm(updated))
                }
            }
            is PaymentMethodsUiEvent.OnCvvChange -> {
                val digitsOnly = event.cvv.filter { it.isDigit() }.take(3)
                _uiState.update {
                    val updated = it.copy(cvv = digitsOnly)
                    updated.copy(isFormValid = validateCardForm(updated))
                }
            }
            is PaymentMethodsUiEvent.OnSaveCard -> saveCard()

            // Card Management
            is PaymentMethodsUiEvent.OnDeleteCard -> deleteCard(event.cardId)
            is PaymentMethodsUiEvent.OnSetDefaultCard -> setDefaultCard(event.cardId)

            // Link E-Wallet Flow
            is PaymentMethodsUiEvent.OnShowLinkWalletSheet -> {
                _uiState.update {
                    it.copy(
                        isLinkWalletSheetVisible = true,
                        linkWalletPhone = "",
                        linkWalletOtp = "",
                        isOtpStep = false
                    )
                }
            }
            is PaymentMethodsUiEvent.OnDismissLinkWalletSheet -> {
                _uiState.update { it.copy(isLinkWalletSheetVisible = false) }
            }
            is PaymentMethodsUiEvent.OnLinkPhoneChange -> {
                val digits = event.phone.filter { it.isDigit() }
                _uiState.update { it.copy(linkWalletPhone = digits) }
            }
            is PaymentMethodsUiEvent.OnLinkOtpChange -> {
                val digits = event.otp.filter { it.isDigit() }.take(6)
                _uiState.update { it.copy(linkWalletOtp = digits) }
            }
            is PaymentMethodsUiEvent.OnRequestOtp -> {
                _uiState.update { it.copy(isOtpStep = true, linkWalletOtp = "123456") }
            }
            is PaymentMethodsUiEvent.OnConfirmLinkWallet -> confirmLinkWallet()
            is PaymentMethodsUiEvent.OnUnlinkWallet -> unlinkWallet()

            // Top-Up Balance Flow
            is PaymentMethodsUiEvent.OnShowTopUpSheet -> {
                _uiState.update { it.copy(isTopUpSheetVisible = true) }
            }
            is PaymentMethodsUiEvent.OnDismissTopUpSheet -> {
                _uiState.update { it.copy(isTopUpSheetVisible = false) }
            }
            is PaymentMethodsUiEvent.OnConfirmTopUp -> processTopUp(event.amount)

            is PaymentMethodsUiEvent.OnNavigateBack -> {}
        }
    }

    private fun validateCardForm(state: PaymentMethodsUiState): Boolean {
        val isCardValid = PaymentValidationUtils.isValidCardNumber(state.cardNumber)
        val isNameValid = state.cardHolder.trim().isNotBlank()
        val isExpiryValid = PaymentValidationUtils.isValidExpiryDate(state.expiryDate)
        val isCvvValid = PaymentValidationUtils.isValidCvv(state.cvv)
        return isCardValid && isNameValid && isExpiryValid && isCvvValid
    }

    // 2. Insert new bank card into Supabase
    private fun saveCard() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val isFirstCard = currentState.savedCards.isEmpty()

            // Format raw 4 digits (e.g. 1228) into MM/YY (12/28) for backend storage
            val rawExpiry = currentState.expiryDate.filter { it.isDigit() }
            val formattedExpiry = if (rawExpiry.length == 4) {
                "${rawExpiry.substring(0, 2)}/${rawExpiry.substring(2)}"
            } else {
                rawExpiry
            }

            val savedDto = paymentRepository.saveBankCard(
                cardHolder = currentState.cardHolder.trim(),
                lastFourDigits = currentState.cardNumber.takeLast(4),
                expiryDate = formattedExpiry,
                isDefault = isFirstCard
            )

            if (savedDto != null) {
                val newCard = SavedBankCard(
                    id = savedDto.id.orEmpty(),
                    cardHolder = savedDto.cardHolder.orEmpty(),
                    lastFourDigits = savedDto.lastFourDigits.orEmpty(),
                    expiryDate = savedDto.expiryDate.orEmpty(),
                    isDefault = savedDto.isDefault
                )

                _uiState.update {
                    it.copy(
                        savedCards = it.savedCards + newCard,
                        isAddingCard = false,
                        cardNumber = "",
                        cardHolder = "",
                        expiryDate = "",
                        cvv = "",
                        isFormValid = false
                    )
                }
            } else {
                val fallbackCard = SavedBankCard(
                    id = System.currentTimeMillis().toString(),
                    cardHolder = currentState.cardHolder.trim(),
                    lastFourDigits = currentState.cardNumber.takeLast(4),
                    expiryDate = formattedExpiry,
                    isDefault = isFirstCard
                )
                _uiState.update {
                    it.copy(
                        savedCards = it.savedCards + fallbackCard,
                        isAddingCard = false,
                        cardNumber = "",
                        cardHolder = "",
                        expiryDate = "",
                        cvv = "",
                        isFormValid = false
                    )
                }
            }
        }
    }

    // 3. Delete card from Supabase
    private fun deleteCard(cardId: String) {
        viewModelScope.launch {
            val success = paymentRepository.deletePaymentMethod(cardId)
            if (success) {
                _uiState.update { current ->
                    val filtered = current.savedCards.filterNot { it.id == cardId }
                    current.copy(savedCards = filtered)
                }
            }
        }
    }

    // 4. Update default card in Supabase
    private fun setDefaultCard(cardId: String) {
        viewModelScope.launch {
            val success = paymentRepository.setDefaultCard(cardId)
            if (success) {
                _uiState.update { current ->
                    current.copy(
                        savedCards = current.savedCards.map { card ->
                            card.copy(isDefault = card.id == cardId)
                        }
                    )
                }
            }
        }
    }

    // 5. Link Touch 'n Go into Supabase
    private fun confirmLinkWallet() {
        val phone = _uiState.value.linkWalletPhone
        viewModelScope.launch {
            val formattedPhone = "+60 ${phone.take(2)}-*** ${phone.takeLast(4)}"
            val success = paymentRepository.linkEWallet(formattedPhone)
            if (success) {
                _uiState.update {
                    it.copy(
                        isTngLinked = true,
                        tngPhone = formattedPhone,
                        isLinkWalletSheetVisible = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isTngLinked = true,
                        tngPhone = formattedPhone,
                        isLinkWalletSheetVisible = false
                    )
                }
            }
        }
    }

    // 6. Delete linked Touch 'n Go from Supabase
    private fun unlinkWallet() {
        viewModelScope.launch {
            val success = paymentRepository.unlinkEWallet()
            if (success) {
                _uiState.update {
                    it.copy(isTngLinked = false, tngPhone = "")
                }
            }
        }
    }

    // 7. Increment wallet_balance in Supabase
    private fun processTopUp(amount: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true) }
            delay(1500)

            val success = paymentRepository.topUpWallet(amount)
            if (success) {
                val updatedBalance = paymentRepository.fetchWalletBalance()
                _uiState.update {
                    it.copy(
                        walletBalance = updatedBalance,
                        isProcessingPayment = false,
                        isTopUpSheetVisible = false
                    )
                }
            } else {
                _uiState.update { it.copy(isProcessingPayment = false) }
            }
        }
    }
}