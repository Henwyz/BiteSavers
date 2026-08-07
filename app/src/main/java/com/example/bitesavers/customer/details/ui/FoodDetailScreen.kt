package com.example.bitesavers.customer.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.customer.details.logic.FoodDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    viewModel: FoodDetailViewModel,
    onBackClick: () -> Unit,
    onReserveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Offer Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                // This single line kills the giant empty gap at the top!
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            if (uiState.offer != null) {
                FoodDetailCheckoutBar(
                    totalPrice = uiState.totalPrice,
                    onReserveClick = {
                        onReserveSuccess()
                    }
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
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error occurred",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                uiState.offer != null -> {
                    val offer = uiState.offer!!
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
                                temperatureText = uiState.temperatureText,
                                isSafe = uiState.isTemperatureSafe
                            )

                            FoodDetailTagsRow(
                                distanceKm = offer.distanceKm
                            )

                            FoodDetailDescription(
                                description = offer.description
                            )

                            FoodDetailQuantitySelector(
                                quantity = uiState.quantity,
                                onIncrease = { viewModel.onIncreaseQuantity() },
                                onDecrease = { viewModel.onDecreaseQuantity() }
                            )

                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 16.dp))
                        }
                    }
                }
            }
        }
    }
}