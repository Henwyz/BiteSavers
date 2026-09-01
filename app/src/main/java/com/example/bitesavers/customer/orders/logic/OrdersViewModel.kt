package com.example.bitesavers.customer.orders.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.orders.data.CustomerOrderItemUiModel
import com.example.bitesavers.customer.orders.data.CustomerOrdersUiState
import com.example.bitesavers.customer.orders.data.OrderStatusType
import com.example.bitesavers.customer.orders.ui.CustomerOrdersUiEvent
import com.example.bitesavers.data.repository.OfferRepository
import com.example.bitesavers.data.repository.OrderRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class OrdersViewModel : ViewModel() {

    private val orderRepository: OrderRepository = OrderRepository()
    private val offerRepository: OfferRepository = OfferRepository()

    private val _uiState = MutableStateFlow(CustomerOrdersUiState(isLoading = true))
    val uiState: StateFlow<CustomerOrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders(showFullLoading = true)
        startOrdersPolling() // <-- Start auto-refresh
    }

    private fun startOrdersPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(5000) // Checks for status changes every 5 seconds
                loadOrders(showFullLoading = false)
            }
        }
    }

    fun onEvent(event: CustomerOrdersUiEvent) {
        when (event) {
            is CustomerOrdersUiEvent.OnTabSelected -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
            }
            is CustomerOrdersUiEvent.OnRefresh -> {
                loadOrders(showFullLoading = true)
            }
            else -> Unit
        }
    }

    // Added showFullLoading flag so polling doesn't trigger the big center spinner
    fun loadOrders(showFullLoading: Boolean = false) {
        viewModelScope.launch {
            if (showFullLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            try {
                val rawOrders = orderRepository.fetchCustomerOrders()

                val mappedList = rawOrders.map { order ->
                    async {
                        val offer = offerRepository.fetchOfferById(order.offerId)
                        val originalTotal = (offer?.originalPrice ?: 0.0) * order.quantity
                        val saved = (originalTotal - order.totalPrice).coerceAtLeast(0.0)

                        val statusType = when (order.status.uppercase()) {
                            "COMPLETED" -> OrderStatusType.COMPLETED
                            "CANCELLED" -> OrderStatusType.CANCELLED
                            else -> OrderStatusType.READY_FOR_PICKUP
                        }

                        val displayDate = order.createdAt?.take(10) ?: "Today"
                        val hasBeenReviewed = !order.remark.isNullOrBlank() && order.remark.contains("Rating:")

                        CustomerOrderItemUiModel(
                            orderId = order.id.orEmpty(),
                            shortOrderId = "#BS-" + (order.id ?: "").takeLast(4).uppercase(),
                            storeName = offer?.storeName ?: "Store",
                            itemName = offer?.title ?: "Food Item",
                            formattedDate = displayDate,
                            totalPrice = order.totalPrice,
                            moneySaved = saved,
                            status = statusType,
                            isReviewed = hasBeenReviewed
                        )
                    }
                }.awaitAll()

                val active = mappedList.filter { it.status == OrderStatusType.READY_FOR_PICKUP }
                val history = mappedList.filter { it.status != OrderStatusType.READY_FOR_PICKUP }
                val completed = history.count { it.status == OrderStatusType.COMPLETED }
                val totalSaved = history.filter { it.status == OrderStatusType.COMPLETED }.sumOf { it.moneySaved }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeOrders = active,
                        historyOrders = history,
                        completedCount = completed,
                        totalSavedAmount = totalSaved
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load orders")
                }
            }
        }
    }
}