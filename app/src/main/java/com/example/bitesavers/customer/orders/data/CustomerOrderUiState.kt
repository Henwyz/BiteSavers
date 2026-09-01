package com.example.bitesavers.customer.orders.data

/**
 * Represents the two tabs at the top of the Orders Screen
 */
enum class OrderTab {
    ACTIVE,
    HISTORY
}

/**
 * Matches the different possible statuses an order can have
 */
enum class OrderStatusType {
    COMPLETED,
    CANCELLED,
    READY_FOR_PICKUP
}

/**
 * UI representation of a single order item card
 */
data class CustomerOrderItemUiModel(
    val orderId: String,
    val shortOrderId: String,
    val storeName: String,
    val itemName: String,
    val formattedDate: String,
    val totalPrice: Double,
    val moneySaved: Double,
    val status: OrderStatusType,
    val isReviewed: Boolean = false
)

/**
 * Whole UI State observed by OrdersScreen
 */
data class CustomerOrdersUiState(
    val isLoading: Boolean = false,
    val selectedTab: OrderTab = OrderTab.ACTIVE,
    val activeOrders: List<CustomerOrderItemUiModel> = emptyList(),
    val historyOrders: List<CustomerOrderItemUiModel> = emptyList(),
    val completedCount: Int = 0,
    val totalSavedAmount: Double = 0.0,
    val errorMessage: String? = null
)