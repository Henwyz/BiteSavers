package com.example.bitesavers.customer.details.ui

import android.content.res.Configuration
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    onReserveSuccess: (String, Int) -> Unit,
    onStoreClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Manages background telemetry polling tied strictly to screen lifecycle
    val currentOfferId = uiState.offer?.id
    DisposableEffect(currentOfferId) {
        if (!currentOfferId.isNullOrBlank()) {
            viewModel.startPolling(currentOfferId)
        }
        onDispose {
            viewModel.stopPolling()
        }
    }

    FoodDetailScreen(
        state = uiState,
        onEvent = { event ->
            // 1. Send every event to the ViewModel
            viewModel.onEvent(event)

            // 2. Handle Navigation triggers
            when (event) {
                is FoodDetailUiEvent.OnNavigateBack -> onBackClick()
                is FoodDetailUiEvent.OnReserveClicked -> {
                    val offerId = uiState.offer?.id
                    if (offerId != null) {
                        onReserveSuccess(offerId, uiState.quantity)
                    }
                }
                is FoodDetailUiEvent.OnStoreClicked -> {
                    val storeId = uiState.offer?.storeId
                    if (!storeId.isNullOrBlank()) {
                        onStoreClick(storeId)
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
    val isNgoClaimActive = state.offer?.isEligibleForNgoFree == true
    // If it is inside the NGO free window, the claim window is active even if regular hours closed
    val isStoreOpen = state.offer?.isCurrentlyOpen ?: true || isNgoClaimActive
    val isExpired = if (isNgoClaimActive) false else (state.offer?.hoursToClose ?: 1) <= 0

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
                    isOpen = isStoreOpen,
                    timeStatusText = if (isNgoClaimActive) stringResource(R.string.status_ngo_free_claim) else state.offer.timeStatusText,
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

                    // Localizes safe holding zone descriptor and temperature banner text
                    val isHot = offer.storageType.contains("Hot", ignoreCase = true)
                    val zoneDescriptor = if (isHot) {
                        stringResource(id = R.string.storage_zone_hot)
                    } else {
                        stringResource(id = R.string.storage_zone_cold)
                    }

                    val dynamicTempText = if (state.isTemperatureSafe) {
                        stringResource(id = R.string.detail_temp_safe_format, offer.liveTemperature, zoneDescriptor)
                    } else {
                        stringResource(id = R.string.detail_temp_breach_format, offer.liveTemperature)
                    }

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
                            FoodDetailHeader(
                                offer = offer,
                                onStoreClick = {
                                    onEvent(FoodDetailUiEvent.OnStoreClicked)
                                }
                            )

                            // Forwards current open condition and timing text to prevent false expired tags
                            FoodDetailStatusRow(
                                hoursToClose = if (isNgoClaimActive) 1 else offer.hoursToClose,
                                stockLeft = offer.quantityLeft,
                                isOpen = isStoreOpen,
                                timeStatusText = if (isNgoClaimActive) stringResource(R.string.status_ngo_free_claim) else offer.timeStatusText
                            )

                            FoodDetailSafetyBanner(
                                temperatureText = dynamicTempText,
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

                            // Display quantity selector if stock is available and either store is open or NGO free claim is active
                            if (!isSoldOut && (!isExpired || isNgoClaimActive)) {
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
@Preview(name = "Food Detail Screen - Populated - Light", showBackground = true)
@Preview(name = "Food Detail Screen - Populated - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FoodDetailScreenPreview() {
    BiteSaversTheme {
        FoodDetailScreen(
            state = FoodDetailUiState(
                isLoading = false,
                isSaved = false,
                quantity = 1,
                totalPrice = 9.90,
                isTemperatureSafe = true,
                offer = OfferUiModel(
                    id = "e3333333-3333-3333-3333-333333333333",
                    storeId = "store_apollo",
                    title = "Chicken Bolognese Pasta",
                    storeName = "Apollo Western & Pasta",
                    storeRating = 4.9,
                    imageResId = R.drawable.food_spaghetti,
                    discountPercent = 45,
                    currentPrice = 9.90,
                    originalPrice = 18.00,
                    distanceKm = 0.3,
                    quantityLeft = 6,
                    hoursToClose = 2,
                    category = DiscoveryCategory.HOT_MEALS,
                    isEligibleForNgoFree = true,
                    liveTemperature = 62.0,
                    storageType = "HOT",
                    description = "Hearty minced chicken pasta in slow-simmered tomato sauce.",
                    timeStatusText = "Closes in 2h",
                    isCurrentlyOpen = true
                )
            ),
            onEvent = {}
        )
    }
}

// Renders the details screen in a Store Closed state
@Preview(name = "Food Detail Screen - Store Closed", showBackground = true)
@Composable
private fun FoodDetailScreenClosedPreview() {
    BiteSaversTheme {
        FoodDetailScreen(
            state = FoodDetailUiState(
                isLoading = false,
                isSaved = false,
                quantity = 1,
                totalPrice = 9.90,
                isTemperatureSafe = true,
                offer = OfferUiModel(
                    id = "e3333333-3333-3333-3333-333333333333",
                    storeId = "store_apollo",
                    title = "Chicken Bolognese Pasta",
                    storeName = "Apollo Western & Pasta",
                    storeRating = 4.9,
                    imageResId = R.drawable.food_spaghetti,
                    discountPercent = 45,
                    currentPrice = 9.90,
                    originalPrice = 18.00,
                    distanceKm = 0.3,
                    quantityLeft = 6,
                    hoursToClose = 0,
                    category = DiscoveryCategory.HOT_MEALS,
                    isEligibleForNgoFree = true,
                    liveTemperature = 60.0,
                    storageType = "HOT",
                    description = "Hearty minced chicken pasta in slow-simmered tomato sauce.",
                    timeStatusText = "Opens in 5h",
                    isCurrentlyOpen = false
                )
            ),
            onEvent = {}
        )
    }
}

// Renders the details screen in a Sold Out state
@Preview(name = "Food Detail Screen - Sold Out", showBackground = true)
@Composable
private fun FoodDetailScreenSoldOutPreview() {
    BiteSaversTheme {
        FoodDetailScreen(
            state = FoodDetailUiState(
                isLoading = false,
                isSaved = true,
                quantity = 0,
                totalPrice = 8.00,
                isTemperatureSafe = true,
                offer = OfferUiModel(
                    id = "e2222222-2222-2222-2222-222222222222",
                    storeId = "store_sweet_treats",
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
                    description = "Delicate layers of crepe with matcha cream.",
                    timeStatusText = "Closes in 2h",
                    isCurrentlyOpen = true
                )
            ),
            onEvent = {}
        )
    }
}

// Shows the loading progress indicator state
@Preview(name = "Food Detail Screen - Loading", showBackground = true)
@Composable
private fun FoodDetailScreenLoadingPreview() {
    BiteSaversTheme {
        FoodDetailScreen(
            state = FoodDetailUiState(isLoading = true),
            onEvent = {}
        )
    }
}