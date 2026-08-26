package com.example.bitesavers.customer.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bitesavers.customer.details.data.FoodDetailUiState
import com.example.bitesavers.customer.details.logic.FoodDetailViewModel
import com.example.bitesavers.customer.details.ui.components.FoodDetailCheckoutBar
import com.example.bitesavers.customer.details.ui.components.FoodDetailDescription
import com.example.bitesavers.customer.details.ui.components.FoodDetailHeader
import com.example.bitesavers.customer.details.ui.components.FoodDetailHero
import com.example.bitesavers.customer.details.ui.components.FoodDetailQuantitySelector
import com.example.bitesavers.customer.details.ui.components.FoodDetailSafetyBanner
import com.example.bitesavers.customer.details.ui.components.FoodDetailStatusRow
import com.example.bitesavers.customer.details.ui.components.FoodDetailTagsRow
import com.example.bitesavers.customer.details.ui.components.FoodDetailTopBar

/**
 * THE ROUTE WRAPPER
 */
@Composable
fun FoodDetailRoute(
    viewModel: FoodDetailViewModel,
    onBackClick: () -> Unit,
    onReserveSuccess: (String, Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FoodDetailScreen(
        state = uiState,
        onEvent = { event ->
            // 1. Send every event to the ViewModel
            viewModel.onEvent(event)

            // 2. Handle Navigation triggers
            when (event) {
                is FoodDetailUiEvent.OnNavigateBack -> onBackClick()
                is FoodDetailUiEvent.OnReserveClicked -> {
                    val currentOfferId = uiState.offer?.id
                    if (currentOfferId != null) {
                        onReserveSuccess(currentOfferId, uiState.quantity)
                    }
                }

                else -> Unit // Quantity changes don't require navigation
            }
        }
    )
}

/**
 * THE STATELESS SCREEN
 */
@Composable
fun FoodDetailScreen(
    state: FoodDetailUiState,
    onEvent: (FoodDetailUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            FoodDetailTopBar(
                isSaved = state.isSaved,
                onBackClick = { onEvent(FoodDetailUiEvent.OnNavigateBack) },
                onBookmarkClick = { onEvent(FoodDetailUiEvent.OnToggleBookmark) }
            )
        },
        bottomBar = {
            if (state.offer != null) {
                FoodDetailCheckoutBar(
                    totalPrice = state.totalPrice,
                    onReserveClick = { onEvent(FoodDetailUiEvent.OnReserveClicked) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                state.offer != null -> {
                    val offer = state.offer
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        FoodDetailHero(
                            imageResId = offer.imageResId
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FoodDetailHeader(offer = offer)

                            FoodDetailStatusRow(
                                hoursToClose = offer.hoursToClose,
                                stockLeft = offer.quantityLeft
                            )

                            FoodDetailSafetyBanner(
                                temperatureText = state.temperatureText,
                                isSafe = state.isTemperatureSafe
                            )

                            FoodDetailTagsRow(
                                distanceKm = offer.distanceKm
                            )

                            FoodDetailDescription(
                                description = offer.description
                            )

                            FoodDetailQuantitySelector(
                                quantity = state.quantity,
                                onIncrease = { onEvent(FoodDetailUiEvent.OnIncreaseQuantity) },
                                onDecrease = { onEvent(FoodDetailUiEvent.OnDecreaseQuantity) }
                            )

                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 16.dp))
                        }
                    }
                }
            }
        }
    }
}