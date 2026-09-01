package com.example.bitesavers.customer.orders.ui

import com.example.bitesavers.customer.orders.data.OrderTab

sealed interface CustomerOrdersUiEvent {
    data class OnTabSelected(val tab: OrderTab) : CustomerOrdersUiEvent
    data class OnOrderClicked(val orderId: String) : CustomerOrdersUiEvent
    data class OnRateOrderClicked(val orderId: String) : CustomerOrdersUiEvent
    data object OnRefresh : CustomerOrdersUiEvent
}