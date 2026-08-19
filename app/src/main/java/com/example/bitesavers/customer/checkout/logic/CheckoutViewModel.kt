package com.example.bitesavers.customer.checkout.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.checkout.data.CheckoutDummyData
import com.example.bitesavers.customer.checkout.data.CheckoutUiState
import com.example.bitesavers.customer.checkout.ui.CheckoutUiEvent
import com.example.bitesavers.data.model.PaymentMethod
import com.example.bitesavers.data.repository.OfferRepository
import com.example.bitesavers.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val offerRepository: OfferRepository = OfferRepository()
    private val orderRepository: OrderRepository = OrderRepository()
    private val _uiState = MutableStateFlow(CheckoutDummyData.initialState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        // 1. Grab the arguments passed from the nav
        val offerId: String? = savedStateHandle.get<String>("offerId")
        val quantity: Int = savedStateHandle.get<Int>("quantity") ?: 1

        if (offerId != null) {
            loadCheckoutDetails(offerId, quantity)
        } else {
            _uiState.update { it.copy(errorMessage = "Invalid Order Details") }
        }
    }

    private fun loadCheckoutDetails(offerId: String, quantity: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 2. Fetch the data from repository (Supabase)
            val offer = offerRepository.fetchOfferById(offerId)

            if (offer != null) {
                _uiState.update { current ->
                    val calculatedTotalPrice = offer.currentPrice * quantity

                    // Automatically default to CASH_ON_PICKUP if wallet balance is insufficient
                    val defaultMethod = if (current.walletBalance >= calculatedTotalPrice) {
                        PaymentMethod.BITESAVER_PAY
                    } else {
                        PaymentMethod.CASH_ON_PICKUP
                    }

                    current.copy(
                        isLoading = false,
                        storeName = offer.storeName,
                        itemName = offer.title,
                        unitPrice = offer.currentPrice,
                        quantity = quantity,
                        selectedPaymentMethod = defaultMethod
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Item no longer available")
                }
            }
        }
    }

    fun onEvent(event: CheckoutUiEvent) {
        when (event) {
            is CheckoutUiEvent.OnNavigateBack -> {
                // Handled in the Route wrapper
            }
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
                        isPaymentSheetVisible = false
                    )
                }
            }
            is CheckoutUiEvent.OnAddNewPaymentClicked -> {
                // Handled at navigation level in CheckoutRoute
            }
            is CheckoutUiEvent.OnConfirmPaymentClicked -> {
                processPayment()
            }
        }
    }

    private fun processPayment() {
        val currentState = _uiState.value

        // Only check in-app wallet balance if BiteSaver Pay is selected
        if (currentState.selectedPaymentMethod == PaymentMethod.BITESAVER_PAY &&
            currentState.walletBalance < currentState.totalPrice
        ) {
            _uiState.update { it.copy(errorMessage = "Insufficient Funds. Please top up your wallet or select Cash on Pickup.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Call Supabase via repository to save the order
            val orderId = orderRepository.placeOrder(
                offerId = savedStateHandle.get<String>("offerId").orEmpty(),
                userRole = "CONSUMER",
                quantity = currentState.quantity,
                totalPrice = currentState.totalPrice,
                hoursToClose = 2, // You can pass dynamic hours if you have them from the offer
                paymentMethod = currentState.selectedPaymentMethod.id
            )

            if (orderId != null) {
                // 2. If success, save the real database orderId and trigger payment success
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        walletBalance = if (current.selectedPaymentMethod == PaymentMethod.BITESAVER_PAY) {
                            current.walletBalance - current.totalPrice
                        } else {
                            current.walletBalance
                        },
                        placedOrderId = orderId, // Stores the UUID returned by Supabase
                        isPaymentSuccessful = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to record order. Please try again.")
                }
            }
        }
    }
}