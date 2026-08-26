package com.example.bitesavers.customer.checkout.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.R
import com.example.bitesavers.customer.checkout.data.CheckoutUiState
import com.example.bitesavers.customer.checkout.ui.CheckoutUiEvent
import com.example.bitesavers.data.model.PaymentMethod
import com.example.bitesavers.data.repository.OfferRepository
import com.example.bitesavers.data.repository.OrderRepository
import com.example.bitesavers.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val offerRepository: OfferRepository = OfferRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CheckoutUiState(
            isLoading = true,
            walletBalance = 0.0
        )
    )
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var currentOfferId: String = ""

    fun loadOffer(offerId: String, quantity: Int = 1) {
        currentOfferId = offerId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorResId = null) }

            val realBalance = paymentRepository.fetchWalletBalance()
            val offer = offerRepository.fetchOfferById(offerId)

            if (offer != null) {
                _uiState.update { current ->
                    val totalPrice = offer.currentPrice * quantity
                    val defaultMethod = if (realBalance >= totalPrice) {
                        PaymentMethod.BITESAVER_PAY
                    } else {
                        PaymentMethod.CASH_ON_PICKUP
                    }

                    current.copy(
                        isLoading = false,
                        walletBalance = realBalance,
                        storeName = offer.storeName,
                        itemName = offer.title,
                        unitPrice = offer.currentPrice,
                        quantity = quantity,
                        selectedPaymentMethod = defaultMethod,
                        errorResId = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        walletBalance = realBalance,
                        errorResId = R.string.error_order_failed_general
                    )
                }
            }
        }
    }

    fun onEvent(event: CheckoutUiEvent) {
        when (event) {
            is CheckoutUiEvent.OnNavigateBack -> {}
            is CheckoutUiEvent.OnChangePaymentClicked -> {
                _uiState.update { it.copy(isPaymentSheetVisible = true) }
            }
            is CheckoutUiEvent.OnDismissPaymentSheet -> {
                _uiState.update { it.copy(isPaymentSheetVisible = false) }
            }
            is CheckoutUiEvent.OnSelectPaymentMethod -> {
                _uiState.update {
                    it.copy(
                        selectedPaymentMethod = event.method,
                        isPaymentSheetVisible = false,
                        errorResId = null
                    )
                }
            }
            is CheckoutUiEvent.OnAddNewPaymentClicked -> {}
            is CheckoutUiEvent.OnConfirmPaymentClicked -> processPayment()
        }
    }

    private fun processPayment() {
        val state = _uiState.value

        // Validate payment methods before placing order
        when (state.selectedPaymentMethod) {
            PaymentMethod.BITESAVER_PAY -> {
                if (state.walletBalance < state.totalPrice) {
                    _uiState.update { it.copy(errorResId = R.string.error_insufficient_bitesaver_balance) }
                    return
                }
            }
            PaymentMethod.TNG_EWALLET -> {
                _uiState.update { it.copy(errorResId = R.string.error_tng_not_linked) }
                return
            }
            PaymentMethod.BANK_CARD -> {
                _uiState.update { it.copy(errorResId = R.string.error_bank_card_not_registered) }
                return
            }
            PaymentMethod.CASH_ON_PICKUP -> {
                // Direct checkout allowed
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorResId = null) }

            val orderId = orderRepository.placeOrder(
                offerId = currentOfferId,
                userRole = "CONSUMER",
                quantity = state.quantity,
                totalPrice = state.totalPrice,
                paymentMethod = state.selectedPaymentMethod.name
            )

            if (orderId != null) {
                val updatedBalance = paymentRepository.fetchWalletBalance()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        walletBalance = updatedBalance,
                        placedOrderId = orderId,
                        isPaymentSuccessful = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorResId = R.string.error_order_failed_general
                    )
                }
            }
        }
    }
}