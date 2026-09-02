package com.example.bitesavers.customer.discovery.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.customer.discovery.data.DiscoveryStoreUiModel
import com.example.bitesavers.customer.discovery.data.DiscoveryUiState
import com.example.bitesavers.customer.discovery.data.DiscoveryViewMode
import com.example.bitesavers.customer.discovery.data.NotificationUiModel
import com.example.bitesavers.customer.discovery.data.UserUiModel
import com.example.bitesavers.customer.discovery.logic.DiscoveryViewModel
import com.example.bitesavers.customer.discovery.logic.LocationUtils.fetchDeviceCoordinates
import com.example.bitesavers.customer.discovery.ui.components.DiscoveryEmptyState
import com.example.bitesavers.customer.discovery.ui.components.DiscoveryFilterRow
import com.example.bitesavers.customer.discovery.ui.components.DiscoveryHeader
import com.example.bitesavers.customer.discovery.ui.components.DiscoveryMapSection
import com.example.bitesavers.customer.discovery.ui.components.DiscoverySearchBar
import com.example.bitesavers.customer.discovery.ui.components.DiscoveryStoreCard
import com.example.bitesavers.customer.sharedUI.OfferCard
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.repository.SavedRepository
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun DiscoveryRoute(
    viewModel: DiscoveryViewModel = viewModel(),
    onOfferClick: (String) -> Unit = {},
    onStoreClick: (String) -> Unit = {},
    onOrderClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 🔄 Automatically reload live inventory every time the user comes back to this screen
    LifecycleResumeEffect(Unit) {
        viewModel.loadOffers()
        viewModel.refreshNotifications()
        onPauseOrDispose { }
    }

    // 🌐 GPS Location Launcher: Requests Fine and Coarse location permissions at runtime
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            fetchDeviceCoordinates(context) { lat, lng ->
                viewModel.updateUserLocation(lat, lng)
            }
        }
    }

    // 🚀 Check or trigger GPS permission request as soon as DiscoveryRoute is entered
    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            fetchDeviceCoordinates(context) { lat, lng ->
                viewModel.updateUserLocation(lat, lng)
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DiscoveryScreen(
        state = uiState,
        onLocationResolved = { lat, lng ->
            viewModel.updateUserLocation(lat, lng)
        },
        onEvent = { event ->
            when (event) {
                is DiscoveryUiEvent.OnSearchQueryChanged ->
                    viewModel.onSearchQueryChanged(event.query)

                is DiscoveryUiEvent.OnCategorySelected ->
                    viewModel.onCategorySelected(event.category)

                is DiscoveryUiEvent.OnViewModeSelected ->
                    viewModel.onViewModeSelected(event.mode)

                is DiscoveryUiEvent.OnOfferClicked ->
                    onOfferClick(event.offer.id)

                is DiscoveryUiEvent.OnStoreClicked ->
                    onStoreClick(event.store.id)

                is DiscoveryUiEvent.OnMapMarkerClicked ->
                    viewModel.onMapMarkerClicked(event.offerId)

                is DiscoveryUiEvent.OnMapOfferNavigate ->
                    onOfferClick(event.offerId)

                is DiscoveryUiEvent.OnRoleChanged ->
                    viewModel.onUserRoleChanged(event.role)

                is DiscoveryUiEvent.OnOpenNotifications ->
                    viewModel.markAllNotificationsAsRead()

                is DiscoveryUiEvent.OnClearAllNotifications ->
                    viewModel.clearAllNotifications()

                is DiscoveryUiEvent.OnNotificationClicked ->
                    onOrderClick(event.orderId)

                is DiscoveryUiEvent.OnResetFilters ->
                    viewModel.onResetFilters()

                is DiscoveryUiEvent.OnToggleBookmark ->
                    viewModel.onEvent(event)
            }
        }
    )
}

@Composable
fun DiscoveryScreen(
    state: DiscoveryUiState,
    onLocationResolved: (Double, Double) -> Unit = { _, _ -> },
    onEvent: (DiscoveryUiEvent) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val savedOfferIds by SavedRepository.savedOfferIds.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // SECTION 1: THE HEADER WITH NOTIFICATION BELL
                item(span = { GridItemSpan(maxLineSpan) }) {
                    DiscoveryHeader(
                        user = state.user,
                        notifications = state.notifications,
                        onOpenNotifications = {
                            onEvent(DiscoveryUiEvent.OnOpenNotifications)
                        },
                        onClearAllNotifications = {
                            onEvent(DiscoveryUiEvent.OnClearAllNotifications)
                        },
                        onNotificationItemClick = { orderId ->
                            onEvent(DiscoveryUiEvent.OnNotificationClicked(orderId))
                        }
                    )
                }

                // SECTION 2: THE MIDDLE CONTENT (Search, Filters, Map)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        DiscoverySearchBar(
                            query = state.searchQuery,
                            onQueryChange = { onEvent(DiscoveryUiEvent.OnSearchQueryChanged(it)) }
                        )

                        DiscoveryFilterRow(
                            availableCategories = state.availableCategories,
                            selectedCategory = state.selectedCategory,
                            onCategorySelected = { onEvent(DiscoveryUiEvent.OnCategorySelected(it)) }
                        )

                        DiscoveryMapSection(
                            markers = state.nearbyMarkers,
                            offers = state.offers.take(3),
                            userRole = state.userRole,
                            userLatitude = state.userLatitude,
                            userLongitude = state.userLongitude,
                            selectedOfferId = state.selectedMapOfferId,
                            onMarkerClick = { onEvent(DiscoveryUiEvent.OnMapMarkerClicked(it)) },
                            onOfferNavigate = { onEvent(DiscoveryUiEvent.OnMapOfferNavigate(it)) },
                            onLocationResolved = onLocationResolved
                        )

                        // Mode toggle bar between offers and stores
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (state.viewMode == DiscoveryViewMode.OFFERS) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
                                    )
                                    .clickable { onEvent(DiscoveryUiEvent.OnViewModeSelected(DiscoveryViewMode.OFFERS)) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.discovery_tab_offers),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (state.viewMode == DiscoveryViewMode.OFFERS) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (state.viewMode == DiscoveryViewMode.STORES) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
                                    )
                                    .clickable { onEvent(DiscoveryUiEvent.OnViewModeSelected(DiscoveryViewMode.STORES)) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.discovery_tab_stores),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (state.viewMode == DiscoveryViewMode.STORES) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = stringResource(id = R.string.discovery_recommended_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // SECTION 3: EMPTY STATE OR THE FOOD/STORE CARDS
                if (state.viewMode == DiscoveryViewMode.OFFERS) {
                    if (state.offers.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            DiscoveryEmptyState(
                                onResetFilters = { onEvent(DiscoveryUiEvent.OnResetFilters) }
                            )
                        }
                    } else {
                        itemsIndexed(state.offers, key = { _, offer -> offer.id }) { index, offer ->
                            val startPadding = if (index % 2 == 0) 16.dp else 0.dp
                            val endPadding = if (index % 2 == 1) 16.dp else 0.dp

                            OfferCard(
                                offer = offer,
                                isSaved = savedOfferIds.contains(offer.id),
                                userRole = state.userRole,
                                modifier = Modifier.padding(start = startPadding, end = endPadding),
                                onClick = { clicked ->
                                    onEvent(DiscoveryUiEvent.OnOfferClicked(clicked))
                                },
                                onToggleBookmark = { offerId ->
                                    onEvent(DiscoveryUiEvent.OnToggleBookmark(offerId))
                                }
                            )
                        }
                    }
                } else {
                    if (state.stores.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            DiscoveryEmptyState(
                                onResetFilters = { onEvent(DiscoveryUiEvent.OnResetFilters) }
                            )
                        }
                    } else {
                        items(state.stores, key = { it.id }, span = { GridItemSpan(maxLineSpan) }) { store ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                DiscoveryStoreCard(
                                    store = store,
                                    onClick = { onEvent(DiscoveryUiEvent.OnStoreClicked(it)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Preview(showBackground = true, name = "Discovery Screen Offers Preview")
@Composable
private fun DiscoveryScreenOffersPreview() {
    BiteSaversTheme {
        DiscoveryScreen(
            state = DiscoveryUiState(
                isLoading = false,
                user = UserUiModel(
                    greeting = "",
                    displayName = "Sarah Tan",
                    avatarInitials = "ST"
                ),
                notifications = listOf(
                    NotificationUiModel(
                        id = "1",
                        orderId = "order_123",
                        title = "Order Ready!",
                        message = "Your food rescue order is ready for pickup.",
                        timestamp = "10m ago",
                        isRead = false
                    )
                ),
                viewMode = DiscoveryViewMode.OFFERS,
                offers = listOf(
                    OfferUiModel(
                        id = "1",
                        title = "Golden Egg Tart Box (4 pcs)",
                        storeName = "Raja Uda Aroma Bakery",
                        storeRating = 4.8,
                        imageResId = R.drawable.food_spaghetti,
                        discountPercent = 50,
                        currentPrice = 8.00,
                        originalPrice = 16.00,
                        distanceKm = 0.3,
                        quantityLeft = 5,
                        hoursToClose = 2,
                        category = DiscoveryCategory.BAKERY,
                        isEligibleForNgoFree = true,
                        liveTemperature = 25.0,
                        storageType = "ROOM_TEMP",
                        description = "Freshly baked egg tarts."
                    )
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Discovery Screen Stores Preview")
@Composable
private fun DiscoveryScreenStoresPreview() {
    BiteSaversTheme {
        DiscoveryScreen(
            state = DiscoveryUiState(
                isLoading = false,
                user = UserUiModel(
                    greeting = "",
                    displayName = "Sarah Tan",
                    avatarInitials = "ST"
                ),
                viewMode = DiscoveryViewMode.STORES,
                stores = listOf(
                    DiscoveryStoreUiModel(
                        id = "1",
                        name = "Raja Uda Aroma Bakery",
                        address = "Penang, Malaysia",
                        rating = 4.8,
                        operatingHours = "Today, 8:00 PM - 9:30 PM",
                        activeOffersCount = 3,
                        distanceKm = 0.3
                    )
                )
            ),
            onEvent = {}
        )
    }
}