package com.example.bitesavers.customer.ticket.data

data class TicketUiState(
    val orderId: String = "",
    val storeName: String = "",
    val pickupWindow: String = "",
    val itemName: String = "",
    val totalPaid: Double = 0.0,
    val savedAmount: Double = 0.0,
    val co2Saved: Double = 0.0,
    val pin: String = "0000",
    val isLoading: Boolean = true
)