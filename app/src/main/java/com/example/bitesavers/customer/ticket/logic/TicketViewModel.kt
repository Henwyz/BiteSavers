package com.example.bitesavers.customer.ticket.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.ticket.data.TicketUiState
import com.example.bitesavers.customer.ticket.ui.TicketUiEvent
import com.example.bitesavers.data.repository.OfferRepository
import com.example.bitesavers.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class TicketViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val offerRepository: OfferRepository = OfferRepository()
    private val orderRepository: OrderRepository = OrderRepository()

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
            val order = orderRepository.fetchOrderById(orderId)
            if (order != null) {
                // 2. Fetch the corresponding offer details
                val offer = offerRepository.fetchOfferById(order.offerId)

                val originalTotal = (offer?.originalPrice ?: 0.0) * order.quantity
                val moneySaved = (originalTotal - order.totalPrice).coerceAtLeast(0.0)

                // Calculates positive food waste / CO2 prevented based on item quantity
                val co2Saved = abs(0.8 * order.quantity)

                // Reads the persistent PIN from Supabase or generates a deterministic fallback
                val pin = order.pickupPin ?: (((abs(orderId.hashCode()) % 9000) + 1000).toString())

                val shortOrderId = "BS-" + orderId.takeLast(5).uppercase()

                // Formats display payment method name cleanly
                val formattedPaymentMethod = when (order.paymentMethod.uppercase()) {
                    "BITESAVER_PAY" -> "BiteSaver Pay"
                    "TNG_EWALLET", "TNG" -> "Touch 'n Go eWallet"
                    "CASH_ON_PICKUP", "CASH" -> "Cash on Pickup"
                    else -> order.paymentMethod
                }

                _uiState.update {
                    it.copy(
                        orderId = shortOrderId,
                        storeName = offer?.storeName ?: "Store",
                        pickupWindow = "Today, until closing",
                        itemName = "${offer?.title ?: "Surprise Bag"} x${order.quantity}",
                        totalPaid = order.totalPrice,
                        paymentMethod = formattedPaymentMethod,
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
                storeName = "Mawar Delights Cafe",
                pickupWindow = "Today, 8:00 PM - 9:30 PM",
                itemName = "Smoked Salmon Salad Bowl x1",
                totalPaid = 11.00,
                paymentMethod = "BiteSaver Pay",
                savedAmount = 11.00,
                co2Saved = 0.8,
                pin = "7667",
                isLoading = false
            )
        }
    }
}