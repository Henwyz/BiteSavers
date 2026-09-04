package com.example.bitesavers.customer.details.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            viewModel.onEvent(event)

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
                else -> Unit
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
    val offer = state.offer
    val isSoldOut = (offer?.quantityLeft ?: 0) <= 0
    val isStoreOpen = offer?.isCurrentlyOpen ?: true
    val isPickupExpired = state.minutesToClose <= 0
    val isNgoClaimEligible = state.isNgoApproved && (offer?.isEligibleForNgoFree == true)

    val canUserReserve = when {
        isSoldOut -> false
        isNgoClaimEligible -> true
        !isStoreOpen -> false
        isPickupExpired -> false
        else -> true
    }

    // Resolves visual pricing with 6% SST added
    val isFreeClaim = isNgoClaimEligible || state.totalPrice <= 0.0
    val visualPriceWithTax = if (isFreeClaim) 0.0 else Math.round(state.totalPrice * 1.06 * 100.0) / 100.0
    val visualTax = if (isFreeClaim) 0.0 else Math.round(state.totalPrice * 0.06 * 100.0) / 100.0

    // Resolves button text
    val dynamicTimingText = state.liveTimeStatus.ifBlank { offer?.timeStatusText ?: "" }
    val actionButtonText = when {
        isSoldOut -> stringResource(R.string.btn_sold_out)
        isNgoClaimEligible -> stringResource(R.string.badge_free_claim)
        !isStoreOpen -> stringResource(R.string.btn_store_closed)
        isPickupExpired && !state.isNgoApproved -> stringResource(R.string.btn_pickup_closed_consumer)
        else -> dynamicTimingText
    }

    Scaffold(
        topBar = {
            FoodDetailTopBar(
                isSaved = state.isSaved,
                onBackClick = { onEvent(FoodDetailUiEvent.OnNavigateBack) },
                onBookmarkClick = { onEvent(FoodDetailUiEvent.OnToggleBookmark) }
            )
        },
        bottomBar = {
            if (offer != null) {
                FoodDetailCheckoutBar(
                    totalPrice = visualPriceWithTax,
                    isSoldOut = isSoldOut,
                    isExpired = !canUserReserve,
                    isOpen = isStoreOpen || isNgoClaimEligible,
                    isNgoFreeClaim = isNgoClaimEligible,
                    timeStatusText = actionButtonText,
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
                offer != null -> {
                    val scrollState = rememberScrollState()

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

                            // Forwards current open condition and minute-level timing text
                            FoodDetailStatusRow(
                                hoursToClose = if (isNgoClaimEligible) 1 else if (state.minutesToClose > 0) 1 else 0,
                                stockLeft = offer.quantityLeft,
                                isOpen = isStoreOpen || isNgoClaimEligible,
                                timeStatusText = if (isNgoClaimEligible) stringResource(R.string.badge_free_claim) else dynamicTimingText
                            )

                            FoodDetailSafetyBanner(
                                temperatureText = dynamicTempText,
                                isSafe = state.isTemperatureSafe
                            )

                            FoodDetailDescription(
                                description = offer.description
                            )

                            // Display quantity selector and visual tax breakdown card
                            if (canUserReserve) {
                                FoodDetailQuantitySelector(
                                    quantity = state.quantity,
                                    onIncrease = { onEvent(FoodDetailUiEvent.OnIncreaseQuantity) },
                                    onDecrease = { onEvent(FoodDetailUiEvent.OnDecreaseQuantity) }
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = stringResource(R.string.subtotal_label_format, state.quantity),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (isFreeClaim) stringResource(R.string.label_free_claim_caps) else stringResource(R.string.currency_rm, state.subtotal),
                                                fontSize = 12.sp,
                                                fontWeight = if (isFreeClaim) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isFreeClaim) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = stringResource(R.string.tax_label_sst),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (isFreeClaim) stringResource(R.string.label_tax_exempt) else stringResource(R.string.currency_rm, visualTax),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = stringResource(R.string.total_title),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isFreeClaim) stringResource(R.string.currency_rm_zero) else stringResource(R.string.currency_rm, visualPriceWithTax),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.padding(bottom = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

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
                totalPrice = 1.00,
                isTemperatureSafe = true,
                isNgoApproved = false,
                minutesToClose = 45,
                liveTimeStatus = "Closes in 45m",
                offer = OfferUiModel(
                    id = "e3333333-3333-3333-3333-333333333333",
                    storeId = "store_apollo",
                    title = "Chicken Bolognese Pasta",
                    storeName = "Apollo Western & Pasta",
                    storeRating = 4.9,
                    imageResId = R.drawable.food_spaghetti,
                    discountPercent = 45,
                    currentPrice = 1.00,
                    originalPrice = 2.00,
                    distanceKm = 0.3,
                    quantityLeft = 6,
                    hoursToClose = 1,
                    category = DiscoveryCategory.HOT_MEALS,
                    isEligibleForNgoFree = true,
                    liveTemperature = 62.0,
                    storageType = "HOT",
                    description = "Hearty minced chicken pasta in slow-simmered tomato sauce.",
                    timeStatusText = "Closes in 45m",
                    isCurrentlyOpen = true
                )
            ),
            onEvent = {}
        )
    }
}