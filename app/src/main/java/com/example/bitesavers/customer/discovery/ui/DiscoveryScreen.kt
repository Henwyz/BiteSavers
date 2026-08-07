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
import androidx.compose.foundation.lazy.grid.itemsIndexed // NEW IMPORT!
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.customer.discovery.data.DiscoveryUiState
import com.example.bitesavers.customer.discovery.logic.DiscoveryViewModel

@Composable
fun DiscoveryRoute(
    viewModel: DiscoveryViewModel = viewModel(),
    onOfferClick: (String) -> Unit = {}
) {
    //Whenever data changes, this triggers a screen redraw automatically
    val uiState by viewModel.uiState.collectAsState()

    DiscoveryScreen(
        state = uiState,
        //the onEvent lambda acts like a switchboard. When a button is clicked in the UI
        //it sends a message up to here, and the Route decides which ViewModel function to trigger
        onEvent = { event ->
            when (event) {
                is DiscoveryUiEvent.OnSearchQueryChanged ->
                    viewModel.onSearchQueryChanged(event.query)

                is DiscoveryUiEvent.OnCategorySelected ->
                    viewModel.onCategorySelected(event.category)

                is DiscoveryUiEvent.OnOfferClicked ->
                    onOfferClick(event.offer.id)

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
                        categories = state.availableCategories,
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = { onEvent(DiscoveryUiEvent.OnCategorySelected(it)) }
                    )

                    DiscoveryMapSection(
                        markers = state.nearbyMarkers
                    )

                    Text(
                        text = "Recommended for you",
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

        //still need a place to show Snackbars, so i align them to the bottom of the Box.
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}