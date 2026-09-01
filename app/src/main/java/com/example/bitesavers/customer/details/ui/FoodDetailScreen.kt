package com.example.bitesavers.customer.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bitesavers.R
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
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.ui.theme.BiteSaversTheme

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
    val isSoldOut = (state.offer?.quantityLeft ?: 0) <= 0
    val isExpired = (state.offer?.hoursToClose ?: 1) <= 0

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
                    isSoldOut = isSoldOut,
                    isExpired = isExpired,
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
                            imageUrl = offer.imageUrl,
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

                            // Dynamic tags: distance, store rating from Supabase, and CO2 savings
                            FoodDetailTagsRow(
                                distanceKm = offer.distanceKm,
                                rating = offer.storeRating
                            )

                            FoodDetailDescription(
                                description = offer.description
                            )

                            // Only display quantity selector if stock is available and offer is active
                            if (!isSoldOut && !isExpired) {
                                FoodDetailQuantitySelector(
                                    quantity = state.quantity,
                                    onIncrease = { onEvent(FoodDetailUiEvent.OnIncreaseQuantity) },
                                    onDecrease = { onEvent(FoodDetailUiEvent.OnDecreaseQuantity) }
                                )
                            }

                            Spacer(modifier = Modifier.padding(bottom = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

// Renders the full details screen with mock offer data in Android Studio preview
@Preview(showBackground = true, name = "Food Detail Screen - Populated")
@Composable
private fun FoodDetailScreenPreview() {
    BiteSaversTheme {
        FoodDetailScreen(
            state = FoodDetailUiState(
                isLoading = false,
                isSaved = false,
                quantity = 1,
                totalPrice = 9.90,
                temperatureText = "25.0°C – within safe room_temp storage zone",
                isTemperatureSafe = true,
                offer = OfferUiModel(
                    id = "e3333333-3333-3333-3333-333333333333",
                    title = "Chicken Bolognese Pasta",
                    storeName = "Apollo Western & Pasta",
                    storeRating = 4.9,
                    imageResId = R.drawable.food_spaghetti,
                    discountPercent = 45,
                    currentPrice = 9.90,
                    originalPrice = 18.00,
                    distanceKm = 0.3,
                    quantityLeft = 6,
                    hoursToClose = 16,
                    category = DiscoveryCategory.HOT_MEALS,
                    isEligibleForNgoFree = true,
                    liveTemperature = 25.0,
                    storageType = "ROOM_TEMP",
                    description = "Hearty minced chicken pasta in slow-simmered tomato sauce."
                )
            ),
            onEvent = {}
        )
    }
}

// Renders the details screen in a Sold Out state
@Preview(showBackground = true, name = "Food Detail Screen - Sold Out")
@Composable
private fun FoodDetailScreenSoldOutPreview() {
    BiteSaversTheme {
        FoodDetailScreen(
            state = FoodDetailUiState(
                isLoading = false,
                isSaved = true,
                quantity = 0,
                totalPrice = 8.00,
                temperatureText = "4.0°C – within safe cold storage zone",
                isTemperatureSafe = true,
                offer = OfferUiModel(
                    id = "e2222222-2222-2222-2222-222222222222",
                    title = "Japanese Matcha Mille Crepe",
                    storeName = "Sweet Treats Cafe",
                    storeRating = 4.9,
                    imageResId = R.drawable.food_spaghetti,
                    discountPercent = 50,
                    currentPrice = 8.00,
                    originalPrice = 16.00,
                    distanceKm = 0.0,
                    quantityLeft = 0,
                    hoursToClose = 2,
                    category = DiscoveryCategory.DESSERTS,
                    isEligibleForNgoFree = false,
                    liveTemperature = 4.0,
                    storageType = "COLD",
                    description = "Delicate layers of crepe with matcha cream."
                )
            ),
            onEvent = {}
        )
    }
}

// Shows the loading progress indicator state
@Preview(showBackground = true, name = "Food Detail Screen - Loading")
@Composable
private fun FoodDetailScreenLoadingPreview() {
    BiteSaversTheme {
        FoodDetailScreen(
            state = FoodDetailUiState(isLoading = true),
            onEvent = {}
        )
    }
}