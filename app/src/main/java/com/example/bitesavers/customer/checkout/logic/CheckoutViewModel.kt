package com.example.bitesavers.customer.checkout.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.checkout.data.CheckoutDummyData
import com.example.bitesavers.customer.checkout.data.CheckoutUiState
import com.example.bitesavers.customer.checkout.ui.CheckoutUiEvent
import com.example.bitesavers.data.repository.OfferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val repository: OfferRepository = OfferRepository()
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
            val offer = repository.fetchOfferById(offerId)

            if (offer != null) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        storeName = offer.storeName,
                        itemName = offer.title,
                        unitPrice = offer.currentPrice,
                        quantity = quantity
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
                // TODO: Open a bottom sheet to change payment method
            }
            is CheckoutUiEvent.OnConfirmPaymentClicked -> {
                processPayment()
            }
        }
    }

    private fun processPayment() {
        val currentState = _uiState.value

        if (currentState.walletBalance < currentState.totalPrice) {
            _uiState.update { it.copy(errorMessage = "Insufficient Funds. Please top up your wallet.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Call Supabase via repository to save the order
            val orderId = repository.placeOrder(
                offerId = savedStateHandle.get<String>("offerId").orEmpty(),
                userRole = "CONSUMER",
                quantity = currentState.quantity,
                totalPrice = currentState.totalPrice,
                hoursToClose = 2 // You can pass dynamic hours if you have them from the offer
            )

            if (orderId != null) {
                // 2. If success, save the real database orderId and trigger payment success
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        walletBalance = current.walletBalance - current.totalPrice,
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