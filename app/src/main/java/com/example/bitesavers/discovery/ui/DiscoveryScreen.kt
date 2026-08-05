package com.example.bitesavers.discovery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.discovery.data.DiscoveryUiState
import com.example.bitesavers.discovery.logic.DiscoveryViewModel

@Composable
fun DiscoveryRoute(
    viewModel: DiscoveryViewModel = viewModel(),
    onOfferClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                DiscoveryHeader(
                    user = state.user,
                    onNotificationClick = {
                        onEvent(DiscoveryUiEvent.OnNotificationClicked)
                    }
                )
            }

            item {
                DiscoverySearchBar(
                    query = state.searchQuery,
                    onQueryChange = { onEvent(DiscoveryUiEvent.OnSearchQueryChanged(it)) }
                )
            }

            item {
                DiscoveryFilterRow(
                    categories = state.availableCategories,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { onEvent(DiscoveryUiEvent.OnCategorySelected(it)) }
                )
            }

            item {
                DiscoveryMapSection(
                    markers = state.nearbyMarkers
                )
            }

            item {
                Text(
                    text = "Recommended for you",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            items(state.filteredOffers, key = { it.id }) { offer ->
                DiscoveryOfferCard(
                    offer = offer,
                    onClick = { clicked ->
                        onEvent(DiscoveryUiEvent.OnOfferClicked(clicked))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}