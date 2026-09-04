package com.example.bitesavers.business.profile.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.business.profile.data.BusinessOwnerAccountUiModel
import com.example.bitesavers.business.profile.data.BusinessProfileUiModel
import com.example.bitesavers.business.profile.logic.BusinessProfileViewModel
import com.example.bitesavers.ui.theme.BiteSaversTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

@Composable
fun BusinessProfileScreen(
    onSignOutClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewWalletClick: (String) -> Unit = {},
    viewModel: BusinessProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val ownerAccount by viewModel.ownerAccount.collectAsStateWithLifecycle()
    val hasPendingBusinessEdit by viewModel.hasPendingBusinessEdit.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    BusinessProfileContent(
        profile = profile,
        ownerAccount = ownerAccount,
        hasPendingBusinessEdit = hasPendingBusinessEdit,
        isLoading = isLoading,
        onSignOutClick = onSignOutClick,
        onEditClick = onEditClick,
        onViewWalletClick = onViewWalletClick
    )
}

@Composable
private fun BusinessProfileContent(
    profile: BusinessProfileUiModel,
    ownerAccount: BusinessOwnerAccountUiModel,
    hasPendingBusinessEdit: Boolean,
    isLoading: Boolean,
    onSignOutClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewWalletClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // ---------- Header ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onSignOutClick,
                    label = {
                        Text(
                            stringResource(R.string.profile_sign_out),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    border = null
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = stringResource(R.string.business_edit_details_title),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { /* Notification center */ },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_notification),
                            contentDescription = stringResource(R.string.cd_notifications),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_store),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    profile.businessName,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                if (ownerAccount.name.isNotBlank()) {
                    Text(
                        "Managed by ${ownerAccount.name}",
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${profile.verificationId} • ${
                        if (profile.isVerified) stringResource(R.string.business_verified_merchant) else ""
                    }",
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star_filled),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${profile.rating} (${profile.reviewCount} ${stringResource(R.string.business_reviews_suffix)})",
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(20.dp)
                .padding(bottom = 80.dp)
        ) {
            // Yellow Pending Banner
            if (hasPendingBusinessEdit) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.business_details_pending_banner),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ---------- Store Wallet Balance Card ----------
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.business_wallet_card_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "RM %.2f".format(profile.walletBalance),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Button(
                        onClick = { onViewWalletClick(profile.storeId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.business_wallet_action_payout),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------- Business info card ----------
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.business_info_title),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    BusinessInfoRow(
                        iconRes = R.drawable.ic_my_location,
                        label = stringResource(R.string.business_address_label),
                        value = profile.address
                    )
                    BusinessInfoRow(
                        iconRes = R.drawable.ic_image,
                        label = stringResource(R.string.business_phone_label),
                        value = profile.phone
                    )
                    BusinessInfoRow(
                        iconRes = R.drawable.ic_clock,
                        label = stringResource(R.string.business_operating_hours_title),
                        value = profile.operatingHours,
                        showDivider = false
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------- MapLibre Map Section (Locked Gestures) ----------
            BusinessStoreMapCard(
                latitude = profile.latitude,
                longitude = profile.longitude,
                storeName = profile.businessName
            )
        }
    }
}

/**
 * Static MapLibre vector map displaying the store's pinned location with gestures disabled.
 */
@Composable
private fun BusinessStoreMapCard(
    latitude: Double,
    longitude: Double,
    storeName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize MapLibre before constructing MapView
    remember {
        try {
            MapLibre.getInstance(context)
        } catch (_: Exception) {}
    }

    // Construct MapView with gestures locked to prevent accidental panning
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                try {
                    // Lock all gestures so the map stays centered on the store coordinates
                    map.uiSettings.apply {
                        isScrollGesturesEnabled = false
                        isZoomGesturesEnabled = false
                        isTiltGesturesEnabled = false
                        isRotateGesturesEnabled = false
                        isDoubleTapGesturesEnabled = false
                        isQuickZoomGesturesEnabled = false
                    }

                    map.setStyle("https://demotiles.maplibre.org/style.json") { _ ->
                        val storePosition = LatLng(latitude, longitude)

                        map.cameraPosition = CameraPosition.Builder()
                            .target(storePosition)
                            .zoom(15.0)
                            .build()

                        try {
                            map.addMarker(
                                MarkerOptions()
                                    .position(storePosition)
                                    .title(storeName)
                            )
                        } catch (_: Exception) {}
                    }
                } catch (_: Exception) {}
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            // Location Chip Overlay
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_my_location),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.business_map_static_label),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun BusinessInfoRow(
    iconRes: Int,
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
        }
    }
    if (showDivider) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }
}

@Preview(name = "Business Profile - Light", showBackground = true)
@Preview(name = "Business Profile - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun BusinessProfileScreenPreview() {
    BiteSaversTheme {
        BusinessProfileContent(
            profile = BusinessProfileUiModel(
                storeId = "store_preview_123",
                businessName = "BiteSaver Kopitiam",
                verificationId = "1426D2B8-2",
                isVerified = true,
                rating = 4.8,
                reviewCount = 115,
                address = "45 Lebuh Chulia, George Town, Penang",
                phone = "+60124567890",
                operatingHours = "08:00:00 - 21:30:00",
                latitude = 5.4164,
                longitude = 100.3327,
                walletBalance = 585.50
            ),
            ownerAccount = BusinessOwnerAccountUiModel(name = "Uncle Ong", email = "ong@gmail.com"),
            hasPendingBusinessEdit = false,
            isLoading = false,
            onSignOutClick = {},
            onEditClick = {},
            onViewWalletClick = {}
        )
    }
}