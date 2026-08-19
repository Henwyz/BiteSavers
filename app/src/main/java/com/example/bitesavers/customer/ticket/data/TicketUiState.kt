package com.example.bitesavers.customer.ticket.data

data class TicketUiState(
    val isLoading: Boolean = false,
    val orderId: String = "",
    val storeName: String = "",
    val pickupWindow: String = "",
    val itemName: String = "",
    val totalPaid: Double = 0.0,
    val savedAmount: Double = 0.0,
    val co2Saved: Double = 0.0,
    val pin: String = "",
    val paymentMethod: String = "BITESAVER_PAY" // 👈 Add this
)