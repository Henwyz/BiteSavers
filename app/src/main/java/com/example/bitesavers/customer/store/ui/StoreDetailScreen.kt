package com.example.bitesavers.customer.store.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.customer.sharedUI.OfferCard
import com.example.bitesavers.customer.store.data.StoreDetailUiModel
import com.example.bitesavers.customer.store.data.StoreDetailUiState
import com.example.bitesavers.customer.store.logic.StoreDetailViewModel
import com.example.bitesavers.customer.store.ui.components.StoreHeaderCard
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun StoreDetailScreen(
    storeId: String,
    userRole: UserRole = UserRole.CONSUMER,
    onBackClick: () -> Unit,
    onOfferClick: (OfferUiModel) -> Unit,
    viewModel: StoreDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(storeId) {
        viewModel.onEvent(StoreDetailUiEvent.LoadStore(storeId))
    }

    StoreDetailScreenContent(
        uiState = uiState,
        userRole = userRole,
        onEvent = { event ->
            when (event) {
                is StoreDetailUiEvent.OnBackClicked -> onBackClick()
                is StoreDetailUiEvent.OnOfferClicked -> onOfferClick(event.offer)
                is StoreDetailUiEvent.OnCallClicked -> dialPhoneNumber(context, event.phoneNumber)
                is StoreDetailUiEvent.OnWhatsAppClicked -> openWhatsApp(context, event.phoneNumber)
                else -> viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreenContent(
    uiState: StoreDetailUiState,
    userRole: UserRole,
    onEvent: (StoreDetailUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.store?.name ?: stringResource(R.string.store_detail_title),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(StoreDetailUiEvent.OnBackClicked) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = stringResource(R.string.store_error_loading, uiState.errorMessage ?: ""),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                uiState.store != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            StoreHeaderCard(
                                store = uiState.store!!,
                                onCallClick = { phone -> onEvent(StoreDetailUiEvent.OnCallClicked(phone)) },
                                onWhatsAppClick = { phone -> onEvent(StoreDetailUiEvent.OnWhatsAppClicked(phone)) }
                            )
                        }

                        item {
                            Text(
                                text = stringResource(R.string.store_active_offers_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }

                        if (uiState.offers.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.store_no_offers),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        } else {
                            items(uiState.offers, key = { it.id }) { offer ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    OfferCard(
                                        offer = offer,
                                        userRole = userRole,
                                        onClick = { onEvent(StoreDetailUiEvent.OnOfferClicked(offer)) },
                                        onToggleBookmark = { id -> onEvent(StoreDetailUiEvent.OnToggleBookmark(id)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Launches native dialer intent for the store contact number
private fun dialPhoneNumber(context: Context, phoneNumber: String?) {
    if (phoneNumber.isNullOrBlank()) {
        Toast.makeText(context, context.getString(R.string.store_contact_not_available), Toast.LENGTH_SHORT).show()
        return
    }
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:${phoneNumber.trim()}")
    }
    context.startActivity(intent)
}

// Launches WhatsApp chat intent for the store contact number
private fun openWhatsApp(context: Context, phoneNumber: String?) {
    if (phoneNumber.isNullOrBlank()) {
        Toast.makeText(context, context.getString(R.string.store_contact_not_available), Toast.LENGTH_SHORT).show()
        return
    }
    val sanitized = phoneNumber.replace("[^0-9]".toRegex(), "")
    val url = "https://api.whatsapp.com/send?phone=$sanitized"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.store_whatsapp_not_installed), Toast.LENGTH_SHORT).show()
    }
}

// ---------- Screen Previews ----------

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun StoreDetailScreenPreview() {
    BiteSaversTheme {
        StoreDetailScreenContent(
            uiState = StoreDetailUiState(
                isLoading = false,
                store = StoreDetailUiModel(
                    id = "store_01",
                    name = "BiteSaver Heritage Kopitiam",
                    address = "45 Lebuh Chulia, George Town, Penang",
                    rating = 4.8,
                    contactPhone = "+60124567890",
                    operatingHours = "Mon – Sun: 8:00 AM - 9:30 PM"
                ),
                offers = listOf(
                    OfferUiModel(
                        id = "off_01",
                        storeId = "store_01",
                        title = "Nasi Lemak & Toast Bundle",
                        storeName = "BiteSaver Heritage Kopitiam",
                        storeRating = 4.8,
                        imageResId = R.drawable.ic_launcher_foreground,
                        imageUrl = null,
                        discountPercent = 52,
                        currentPrice = 8.50,
                        originalPrice = 18.00,
                        distanceKm = 1.2,
                        quantityLeft = 4,
                        hoursToClose = 2,
                        pickupWindow = "Today, 8:00 PM - 10:00 PM",
                        category = DiscoveryCategory.HOT_MEALS,
                        isEligibleForNgoFree = true,
                        liveTemperature = 60.0,
                        storageType = "HOT",
                        description = "Fresh surplus chicken rendang bento box."
                    )
                )
            ),
            userRole = UserRole.CONSUMER,
            onEvent = {}
        )
    }
}