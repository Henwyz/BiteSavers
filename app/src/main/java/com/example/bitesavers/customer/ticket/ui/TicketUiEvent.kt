package com.example.bitesavers.customer.ticket.ui

sealed interface TicketUiEvent {
    object OnBackClick : TicketUiEvent
}