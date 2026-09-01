package com.example.bitesavers.customer.ticket.ui

sealed interface TicketUiEvent {
    data object OnBackClick : TicketUiEvent
    data object OnDismissReviewSheet : TicketUiEvent
    data class OnSubmitReview(val rating: Int, val comment: String) : TicketUiEvent
}