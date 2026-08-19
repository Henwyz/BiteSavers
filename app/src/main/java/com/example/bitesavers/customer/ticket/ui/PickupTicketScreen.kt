package com.example.bitesavers.customer.ticket.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.customer.ticket.data.TicketUiState
import com.example.bitesavers.customer.ticket.logic.TicketViewModel
import com.example.bitesavers.customer.ticket.ui.components.PickupTicketHeader
import com.example.bitesavers.customer.ticket.ui.components.PickupTicketImpactCard
import com.example.bitesavers.customer.ticket.ui.components.PickupTicketPaymentCard
import com.example.bitesavers.customer.ticket.ui.components.PickupTicketPinCard
import com.example.bitesavers.customer.ticket.ui.components.PickupTicketSummaryCard
import com.example.bitesavers.ui.theme.BiteSaversTheme

/**
 * THE ROUTE WRAPPER
 * This handles the ViewModel, observes state, and manages Navigation.
 */
@Composable
fun TicketRoute(
    viewModel: TicketViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PickupTicketScreen(
        state = uiState,
        onEvent = { event ->
            // 1. Pass the event to the ViewModel
            viewModel.onEvent(event)

            // 2. Trigger navigation if the event requires it
            when (event) {
                is TicketUiEvent.OnBackClick -> onNavigateBack()
            }
        }
    )
}

@Composable
fun PickupTicketScreen(
    state: TicketUiState,
    onEvent: (TicketUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PickupTicketHeader(
            onBackClick = { onEvent(TicketUiEvent.OnBackClick) }
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PickupTicketPinCard(pin = state.pin)

                PickupTicketSummaryCard(
                    orderId = state.orderId,
                    storeName = state.storeName,
                    pickupWindow = state.pickupWindow,
                    itemName = state.itemName
                )

                PickupTicketPaymentCard(
                    totalPaid = state.totalPaid,
                    paymentMethod = state.paymentMethod
                )

                PickupTicketImpactCard(
                    savedAmount = state.savedAmount,
                    co2Saved = state.co2Saved
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PickupTicketPreview() {
    BiteSaversTheme {
        PickupTicketScreen(
            state = TicketUiState(
                orderId = "BS-28401",
                storeName = "Madam Lim Bakery",
                pickupWindow = "4:00 - 6:00 PM",
                itemName = "Butter Croissant",
                totalPaid = 5.00,
                savedAmount = 5.00,
                co2Saved = 0.8,
                pin = "7667",
                paymentMethod = "TNG_EWALLET"
            ),
            onEvent = {}
        )
    }
}