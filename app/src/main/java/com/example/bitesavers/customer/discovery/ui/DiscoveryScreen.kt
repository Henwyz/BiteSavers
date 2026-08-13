package com.example.bitesavers.customer.discovery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource // NEW IMPORT
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R // NEW IMPORT
import com.example.bitesavers.customer.discovery.data.DiscoveryUiState
import com.example.bitesavers.customer.discovery.logic.DiscoveryViewModel
import com.example.bitesavers.customer.discovery.ui.components.DiscoveryFilterRow
import com.example.bitesavers.customer.discovery.ui.components.DiscoveryHeader
import com.example.bitesavers.customer.discovery.ui.components.DiscoveryMapSection
import com.example.bitesavers.customer.discovery.ui.components.DiscoveryOfferCard
import com.example.bitesavers.customer.discovery.ui.components.DiscoverySearchBar

@Composable
fun DiscoveryRoute(
    viewModel: DiscoveryViewModel = viewModel(),
    onOfferClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DiscoveryScreen(
        state = uiState,
        onEvent = { event ->
            when (event) {
                is DiscoveryUiEvent.OnSearchQueryChanged ->
                    viewModel.onSearchQueryChanged(event.query)

                is DiscoveryUiEvent.OnCategorySelected ->
                    viewModel.onCategorySelected(event.category)

                is DiscoveryUiEvent.OnOfferClicked ->
                    onOfferClick(event.offer.id)

                // NEW: Handle Map Marker Clicks (Make sure to add this to your sealed class!)
                is DiscoveryUiEvent.OnMapMarkerClicked ->
                    viewModel.onMapMarkerClicked(event.offerId)

                // NEW: Handle Navigation from the Map Popup (Add this to your sealed class too!)
                is DiscoveryUiEvent.OnMapOfferNavigate ->
                    onOfferClick(event.offerId)

                is DiscoveryUiEvent.OnRoleChanged ->
                    viewModel.onUserRoleChanged(event.role)

                DiscoveryUiEvent.OnNotificationClicked -> {
                    // TODO: navigate to notifications
                }
            }
        }
    )
}

@Composable
fun DiscoveryScreen(
    state: DiscoveryUiState,
    onEvent: (DiscoveryUiEvent) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SECTION 1: THE HEADER
            item(span = { GridItemSpan(maxLineSpan) }) {
                DiscoveryHeader(
                    user = state.user,
                    location = "Kuala Lumpur", // UPDATED: Passed a temporary location string!
                    onNotificationClick = {
                        onEvent(DiscoveryUiEvent.OnNotificationClicked)
                    }
                )
            }

            // SECTION 2: THE MIDDLE CONTENT
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
                        offers = state.filteredOffers,
                        userRole = state.userRole,
                        selectedOfferId = state.selectedMapOfferId,
                        onMarkerClick = { onEvent(DiscoveryUiEvent.OnMapMarkerClicked(it)) },
                        onOfferNavigate = { onEvent(DiscoveryUiEvent.OnMapOfferNavigate(it)) }
                    )

                    Text(
                        text = stringResource(id = R.string.discovery_recommended_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // SECTION 3: THE FOOD CARDS
            itemsIndexed(state.filteredOffers, key = { _, offer -> offer.id }) { index, offer ->
                val startPadding = if (index % 2 == 0) 16.dp else 0.dp
                val endPadding = if (index % 2 == 1) 16.dp else 0.dp

                DiscoveryOfferCard(
                    offer = offer,
                    modifier = Modifier.padding(start = startPadding, end = endPadding),
                    onClick = { clicked ->
                        onEvent(DiscoveryUiEvent.OnOfferClicked(clicked))
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}