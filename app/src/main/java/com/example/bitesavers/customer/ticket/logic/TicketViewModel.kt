package com.example.bitesavers.customer.ticket.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.ticket.data.TicketUiState
import com.example.bitesavers.customer.ticket.ui.TicketUiEvent
import com.example.bitesavers.data.repository.OfferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicketViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository: OfferRepository = OfferRepository()

    private val _uiState = MutableStateFlow(TicketUiState(isLoading = true))
    val uiState: StateFlow<TicketUiState> = _uiState.asStateFlow()

    init {
        val orderId: String? = savedStateHandle.get<String>("orderId")
        if (!orderId.isNullOrBlank()) {
            loadTicketDetails(orderId)
        } else {
            generateDummyTicket()
        }
    }

    fun onEvent(event: TicketUiEvent) {
        when (event) {
            is TicketUiEvent.OnBackClick -> {
                // Handled at navigation level
            }
        }
    }

    private fun loadTicketDetails(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Fetch the order record from Supabase
            val order = repository.fetchOrderById(orderId)
            if (order != null) {
                // 2. Fetch the corresponding offer details
                val offer = repository.fetchOfferById(order.offerId)

                val originalTotal = (offer?.originalPrice ?: 0.0) * order.quantity
                val moneySaved = (originalTotal - order.totalPrice).coerceAtLeast(0.0)
                val co2Saved = 0.8 * order.quantity

                // Generate a 4-digit numeric pickup PIN from orderId
                val pin = (orderId.hashCode() % 9000 + 1000).let { if (it < 0) it * -1 else it }.toString()
                val shortOrderId = "BS-" + orderId.takeLast(5).uppercase()

                _uiState.update {
                    it.copy(
                        orderId = shortOrderId,
                        storeName = offer?.storeName ?: "Store",
                        pickupWindow = order.pickupWindowClose,
                        itemName = offer?.title ?: "Surprise Bag",
                        totalPaid = order.totalPrice,
                        savedAmount = moneySaved,
                        co2Saved = co2Saved,
                        pin = pin,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun generateDummyTicket() {
        _uiState.update { currentState ->
            currentState.copy(
                orderId = "BS-28401",
                storeName = "Madam Lim Bakery",
                pickupWindow = "4:00 - 6:00 PM",
                itemName = "Butter Croissant",
                totalPaid = 5.00,
                savedAmount = 5.00,
                co2Saved = 0.8,
                pin = "7667",
                isLoading = false
            )
        }
    }
}