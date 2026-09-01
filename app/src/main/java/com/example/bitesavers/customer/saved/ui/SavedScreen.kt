package com.example.bitesavers.customer.saved.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.customer.saved.data.SavedUiState
import com.example.bitesavers.customer.saved.logic.SavedViewModel
import com.example.bitesavers.customer.saved.ui.components.SavedOfferCard
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.ui.theme.BiteSaversTheme

/**
 * THE ROUTE WRAPPER
 */
@Composable
fun SavedRoute(
    viewModel: SavedViewModel = viewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SavedScreen(
        state = uiState,
        onEvent = { event ->
            viewModel.onEvent(event)
            when (event) {
                is SavedUiEvent.OnOfferClicked -> onNavigateToDetail(event.offerId)
                else -> {}
            }
        }
    )
}

/**
 * THE STATELESS SCREEN
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    state: SavedUiState,
    onEvent: (SavedUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.saved_screen_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    windowInsets = WindowInsets(0.dp)
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
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
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.savedOffers.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_saved),
                            contentDescription = stringResource(R.string.cd_bookmark_icon),
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.saved_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.saved_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.savedOffers,
                            key = { it.id }
                        ) { offer ->
                            SavedOfferCard(
                                offer = offer,
                                onOfferClick = { onEvent(SavedUiEvent.OnOfferClicked(it)) },
                                onRemoveClick = { onEvent(SavedUiEvent.OnToggleBookmark(it)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Renders saved items list preview with sample data
@Preview(showBackground = true, name = "Saved Screen - Populated")
@Composable
private fun SavedScreenPopulatedPreview() {
    BiteSaversTheme {
        SavedScreen(
            state = SavedUiState(
                isLoading = false,
                savedOffers = listOf(
                    OfferUiModel(
                        id = "e1",
                        title = "Chicken Bolognese Pasta",
                        storeName = "Apollo Western & Pasta",
                        storeRating = 4.9,
                        imageResId = R.drawable.food_spaghetti,
                        discountPercent = 45,
                        currentPrice = 9.90,
                        originalPrice = 18.00,
                        distanceKm = 0.0,
                        quantityLeft = 6,
                        hoursToClose = 15,
                        category = DiscoveryCategory.HOT_MEALS,
                        isEligibleForNgoFree = true,
                        liveTemperature = 25.0,
                        storageType = "ROOM_TEMP",
                        description = "Hearty minced chicken pasta in slow-simmered tomato sauce."
                    )
                )
            ),
            onEvent = {}
        )
    }
}

// Renders empty saved items preview
@Preview(showBackground = true, name = "Saved Screen - Empty")
@Composable
private fun SavedScreenEmptyPreview() {
    BiteSaversTheme {
        SavedScreen(
            state = SavedUiState(
                isLoading = false,
                savedOffers = emptyList()
            ),
            onEvent = {}
        )
    }
}

// Renders loading indicator preview
@Preview(showBackground = true, name = "Saved Screen - Loading")
@Composable
private fun SavedScreenLoadingPreview() {
    BiteSaversTheme {
        SavedScreen(
            state = SavedUiState(
                isLoading = true,
                savedOffers = emptyList()
            ),
            onEvent = {}
        )
    }
}