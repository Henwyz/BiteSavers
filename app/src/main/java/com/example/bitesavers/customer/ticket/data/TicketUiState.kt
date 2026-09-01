package com.example.bitesavers.customer.ticket.data

data class TicketUiState(
    val orderId: String = "",
    val rawOrderId: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val pickupWindow: String = "",
    val itemName: String = "",
    val totalPaid: Double = 0.0,
    val paymentMethod: String = "",
    val savedAmount: Double = 0.0,
    val co2Saved: Double = 0.0,
    val pin: String = "",
    val orderStatus: String = "READY_FOR_PICKUP",
    val isCompleted: Boolean = false,
    val showReviewSheet: Boolean = false,
    val isReviewSubmitted: Boolean = false,
    val isLoading: Boolean = false
)