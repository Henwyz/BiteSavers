package com.example.bitesavers.customer.ticket.logic

import androidx.lifecycle.ViewModel
import com.example.bitesavers.customer.ticket.data.TicketUiState
import com.example.bitesavers.customer.ticket.ui.TicketUiEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TicketViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TicketUiState())
    val uiState: StateFlow<TicketUiState> = _uiState.asStateFlow()

    init {
        generateDummyTicket()
    }

    fun onEvent(event: TicketUiEvent) {
        when (event) {
            is TicketUiEvent.OnBackClick -> {
                // Handle any ViewModel-specific cleanup here if necessary
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