package com.example.bitesavers.customer.checkout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.customer.checkout.data.CheckoutUiState
import com.example.bitesavers.customer.checkout.logic.CheckoutViewModel
import com.example.bitesavers.customer.checkout.ui.components.CheckoutBottomBar
import com.example.bitesavers.customer.checkout.ui.components.CheckoutPaymentCard
import com.example.bitesavers.customer.checkout.ui.components.CheckoutSummaryCard
import com.example.bitesavers.customer.checkout.ui.components.CheckoutTopBar
import com.example.bitesavers.ui.theme.BiteSaversTheme

/**
 * THE ROUTE WRAPPER
 */
@Composable
fun CheckoutRoute(
    viewModel: CheckoutViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onCheckoutSuccess: () -> Unit // e.g., Navigate to Ticket Screen
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Listen for the payment success flag to trigger navigation safely
    LaunchedEffect(uiState.isPaymentSuccessful) {
        if (uiState.isPaymentSuccessful) {
            onCheckoutSuccess()
        }
    }

    CheckoutScreen(
        state = uiState,
        onEvent = { event ->
            viewModel.onEvent(event) // Send all events to ViewModel

            // Handle pure navigation events locally
            if (event is CheckoutUiEvent.OnNavigateBack) {
                onNavigateBack()
            }
        }
    )
}

/**
 * THE STATELESS SCREEN
 */
@Composable
fun CheckoutScreen(
    state: CheckoutUiState,
    onEvent: (CheckoutUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CheckoutTopBar(
                onBackClick = { onEvent(CheckoutUiEvent.OnNavigateBack) }
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                totalPrice = state.totalPrice,
                onConfirmClick = { onEvent(CheckoutUiEvent.OnConfirmPaymentClicked) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Swapped the hardcoded hex for your dynamic background theme role
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CheckoutSummaryCard(
                    storeName = state.storeName,
                    itemName = state.itemName,
                    quantity = state.quantity,
                    unitPrice = state.unitPrice
                )

                CheckoutPaymentCard(
                    walletBalance = state.walletBalance,
                    onChangeClick = { onEvent(CheckoutUiEvent.OnChangePaymentClicked) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckoutScreenPreview() {
    BiteSaversTheme {
        CheckoutScreen(
            state = CheckoutUiState(
                storeName = "Artisan Bakery",
                itemName = "Butter Croissant",
                quantity = 2,
                unitPrice = 2.50,
                walletBalance = 45.50
            ),
            onEvent = {}
        )
    }
}