package com.example.bitesavers.customer.orders.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.customer.orders.data.CustomerOrderItemUiModel
import com.example.bitesavers.customer.orders.data.CustomerOrdersUiState
import com.example.bitesavers.customer.orders.data.OrderStatusType
import com.example.bitesavers.customer.orders.data.OrderTab
import com.example.bitesavers.customer.orders.logic.OrdersViewModel
import com.example.bitesavers.customer.orders.ui.components.OrderHistoryCard
import com.example.bitesavers.customer.orders.ui.components.OrderTabSelector
import com.example.bitesavers.customer.orders.ui.components.OrdersHeader
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun OrdersRoute(
    viewModel: OrdersViewModel = viewModel(),
    onOrderClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OrdersScreen(
        state = uiState,
        onEvent = { event ->
            when (event) {
                is CustomerOrdersUiEvent.OnTabSelected ->
                    viewModel.onEvent(event)

                is CustomerOrdersUiEvent.OnRefresh ->
                    viewModel.onEvent(event)

                is CustomerOrdersUiEvent.OnOrderClicked ->
                    onOrderClick(event.orderId)

                else -> Unit
            }
        }
    )
}

@Composable
fun OrdersScreen(
    state: CustomerOrdersUiState,
    onEvent: (CustomerOrdersUiEvent) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                OrdersHeader(
                    completedCount = state.completedCount,
                    totalSavedAmount = state.totalSavedAmount
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TAB SELECTOR (Active / History)
            OrderTabSelector(
                selectedTab = state.selectedTab,
                onTabSelected = { tab ->
                    onEvent(CustomerOrdersUiEvent.OnTabSelected(tab))
                }
            )

            // CONTENT SECTION
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    state.errorMessage != null -> {
                        Text(
                            text = state.errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 24.dp)
                        )
                    }

                    else -> {
                        val currentList = if (state.selectedTab == OrderTab.ACTIVE) {
                            state.activeOrders
                        } else {
                            state.historyOrders
                        }

                        if (currentList.isEmpty()) {
                            Text(
                                text = if (state.selectedTab == OrderTab.ACTIVE) {
                                    stringResource(id = R.string.orders_empty_active)
                                } else {
                                    stringResource(id = R.string.orders_empty_history)
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 8.dp,
                                    bottom = 24.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(currentList, key = { it.orderId }) { order ->
                                    OrderHistoryCard(
                                        order = order,
                                        onClick = {
                                            onEvent(CustomerOrdersUiEvent.OnOrderClicked(order.orderId))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= Previews =================

@Preview(name = "Orders Screen - History Tab", showBackground = true)
@Composable
private fun OrdersScreenHistoryPreview() {
    val sampleOrders = listOf(
        CustomerOrderItemUiModel(
            orderId = "1",
            shortOrderId = "#BS-0701",
            storeName = "Old Town Restaurant",
            itemName = "Nasi Lemak Set",
            formattedDate = "10 Jul 2026",
            totalPrice = 4.50,
            moneySaved = 4.50,
            status = OrderStatusType.COMPLETED
        ),
        CustomerOrderItemUiModel(
            orderId = "2",
            shortOrderId = "#BS-0700",
            storeName = "Madam Lim Bakery",
            itemName = "Assorted Kuih Box",
            formattedDate = "9 Jul 2026",
            totalPrice = 2.50,
            moneySaved = 5.00,
            status = OrderStatusType.COMPLETED
        )
    )

    BiteSaversTheme {
        OrdersScreen(
            state = CustomerOrdersUiState(
                isLoading = false,
                selectedTab = OrderTab.HISTORY,
                historyOrders = sampleOrders,
                completedCount = 2,
                totalSavedAmount = 9.50
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Orders Screen - Empty State", showBackground = true)
@Composable
private fun OrdersScreenEmptyPreview() {
    BiteSaversTheme {
        OrdersScreen(
            state = CustomerOrdersUiState(
                isLoading = false,
                selectedTab = OrderTab.ACTIVE,
                activeOrders = emptyList(),
                completedCount = 0,
                totalSavedAmount = 0.0
            ),
            onEvent = {}
        )
    }
}