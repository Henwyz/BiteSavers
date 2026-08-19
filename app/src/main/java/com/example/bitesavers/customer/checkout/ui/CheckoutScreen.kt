package com.example.bitesavers.customer.checkout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator // Added
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text // Added
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment // Added
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
import com.example.bitesavers.customer.checkout.ui.components.PaymentMethodSelectionSheet // 👈 Added
import com.example.bitesavers.data.model.PaymentMethod
import com.example.bitesavers.ui.theme.BiteSaversTheme

/**
 * THE ROUTE WRAPPER
 */
@Composable
fun CheckoutRoute(
    viewModel: CheckoutViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit, // 👈 Add parameter
    onCheckoutSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isPaymentSuccessful, uiState.placedOrderId) {
        if (uiState.isPaymentSuccessful && uiState.placedOrderId != null) {
            onCheckoutSuccess(uiState.placedOrderId!!)
        }
    }

    CheckoutScreen(
        state = uiState,
        onEvent = { event ->
            viewModel.onEvent(event)

            when (event) {
                is CheckoutUiEvent.OnNavigateBack -> onNavigateBack()
                is CheckoutUiEvent.OnAddNewPaymentClicked -> onNavigateToPaymentMethods()
                else -> {}
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
            // Only show the pay button if we aren't loading and there are no errors
            if (!state.isLoading && state.errorMessage == null) {
                CheckoutBottomBar(
                    totalPrice = state.totalPrice,
                    onConfirmClick = { onEvent(CheckoutUiEvent.OnConfirmPaymentClicked) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            //handle loading, error and success states
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }

                else -> {
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
                            paymentMethod = state.selectedPaymentMethod,
                            onChangeClick = { onEvent(CheckoutUiEvent.OnChangePaymentClicked) }
                        )
                    }
                }
            }

            //Show Payment Method Bottom Sheet when state.isPaymentSheetVisible == true
            if (state.isPaymentSheetVisible) {
                PaymentMethodSelectionSheet(
                    selectedMethod = state.selectedPaymentMethod,
                    onMethodSelect = { method ->
                        onEvent(CheckoutUiEvent.OnSelectPaymentMethod(method))
                    },
                    onAddNewPaymentClick = {
                        onEvent(CheckoutUiEvent.OnDismissPaymentSheet)
                        onEvent(CheckoutUiEvent.OnAddNewPaymentClicked)
                    },
                    onDismiss = {
                        onEvent(CheckoutUiEvent.OnDismissPaymentSheet)
                    }
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
                walletBalance = 45.50,
                selectedPaymentMethod = PaymentMethod.BITESAVER_PAY
            ),
            onEvent = {}
        )
    }
}