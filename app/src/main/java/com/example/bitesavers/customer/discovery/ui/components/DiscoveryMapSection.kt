package com.example.bitesavers.customer.discovery.ui.components

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bitesavers.R
import com.example.bitesavers.customer.discovery.data.NearbyDealMarkerUiModel
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole
import com.example.bitesavers.ui.theme.BiteSaversTheme

// MapLibre Imports
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode

@Composable
fun DiscoveryMapSection(
    markers: List<NearbyDealMarkerUiModel>,
    offers: List<OfferUiModel> = emptyList(),
    userRole: UserRole = UserRole.CONSUMER,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    selectedOfferId: String?,
    onMarkerClick: (String?) -> Unit,
    onOfferNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isInPreview = LocalInspectionMode.current
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .height(if (isExpanded) 420.dp else 210.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        if (isInPreview) {
            DiscoveryMapPlaceholder(
                markers = markers,
                onMarkerClick = onMarkerClick
            )
        } else {
            DiscoveryMapMapLibre(
                markers = markers,
                userLatitude = userLatitude,
                userLongitude = userLongitude,
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = !isExpanded },
                onMarkerClick = onMarkerClick
            )
        }

        // --- THE POPUP OVERLAY CARD / MULTI-ITEM CAROUSEL ---
        if (selectedOfferId != null) {
            val selectedMarker = markers.find { it.storeId == selectedOfferId }
            val storeOffers = selectedMarker?.offers ?: offers.filter { it.id == selectedOfferId }

            if (storeOffers.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .fillMaxWidth()
                ) {
                    if (storeOffers.size == 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CompactDiscoveryOfferCard(
                                offer = storeOffers.first(),
                                userRole = userRole,
                                onClick = { onOfferNavigate(it.id) }
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            items(storeOffers, key = { it.id }) { offer ->
                                CompactDiscoveryOfferCard(
                                    offer = offer,
                                    userRole = userRole,
                                    modifier = Modifier.width(240.dp),
                                    onClick = { onOfferNavigate(offer.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DiscoveryMapMapLibre(
    markers: List<NearbyDealMarkerUiModel>,
    userLatitude: Double?,
    userLongitude: Double?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onMarkerClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val styleUrl = "https://api.maptiler.com/maps/streets-v4/style.json?key=3OJ2B5f1qI0Cqbcpt5xf"
    val context = LocalContext.current

    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                mapView.apply {
                    setOnTouchListener { view, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN,
                            MotionEvent.ACTION_MOVE -> {
                                view.parent.requestDisallowInterceptTouchEvent(true)
                            }
                            MotionEvent.ACTION_UP,
                            MotionEvent.ACTION_CANCEL -> {
                                view.parent.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                        false
                    }

                    getMapAsync { map: MapLibreMap ->
                        mapInstance = map
                        map.setStyle(Style.Builder().fromUri(styleUrl)) { style: Style ->
                            try {
                                val locationComponent = map.locationComponent
                                val options = LocationComponentActivationOptions
                                    .builder(context, style)
                                    .useDefaultLocationEngine(true)
                                    .build()
                                locationComponent.activateLocationComponent(options)
                                locationComponent.isLocationComponentEnabled = true
                                locationComponent.cameraMode = CameraMode.TRACKING
                                locationComponent.renderMode = RenderMode.COMPASS
                                locationComponent.zoomWhileTracking(15.5)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            // Center immediately on user position when style loads
                            if (userLatitude != null && userLongitude != null) {
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(userLatitude, userLongitude),
                                        15.5
                                    )
                                )
                            }

                            map.setOnMarkerClickListener { marker ->
                                val clickedStoreId = marker.snippet
                                if (clickedStoreId != null) {
                                    map.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            marker.position,
                                            16.5
                                        ),
                                        400
                                    )
                                    onMarkerClick(clickedStoreId)
                                    true
                                } else {
                                    false
                                }
                            }

                            map.addOnMapClickListener {
                                onMarkerClick(null)
                                false
                            }
                        }
                    }
                }
            },
            update = { view ->
                view.getMapAsync { map ->
                    if (map.style != null) {
                        map.clear()
                        markers.forEach { markerData ->
                            map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(markerData.latitude, markerData.longitude))
                                    .title(markerData.labelText)
                                    .snippet(markerData.storeId)
                            )
                        }
                    }
                }
            }
        )

        // Floating Action Controls: Recenter & Expand Buttons
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Recenter to user's live position
            IconButton(
                onClick = {
                    if (userLatitude != null && userLongitude != null && mapInstance != null) {
                        mapInstance?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(userLatitude, userLongitude),
                                15.5
                            ),
                            600
                        )
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Expand / Collapse map toggle
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Expand Map",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DiscoveryMapPlaceholder(
    markers: List<NearbyDealMarkerUiModel>,
    onMarkerClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onMarkerClick(null) }
    ) {
        Text(
            text = stringResource(id = R.string.map_preview_placeholder),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            markers.take(3).forEach {
                Text(
                    text = it.labelText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onMarkerClick(it.storeId) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoveryMapSectionPreview() {
    BiteSaversTheme {
        DiscoveryMapSection(
            markers = listOf(
                NearbyDealMarkerUiModel(
                    storeId = "s1",
                    storeName = "Bakery Delights",
                    labelText = "2 DEALS",
                    latitude = 3.1466,
                    longitude = 101.6958,
                    offers = listOf(
                        OfferUiModel(
                            id = "1",
                            title = "Butter Croissant",
                            storeName = "Bakery Delights",
                            imageResId = R.drawable.food_spaghetti,
                            discountPercent = 30,
                            currentPrice = 4.50,
                            originalPrice = 6.50,
                            distanceKm = 0.5,
                            quantityLeft = 5,
                            hoursToClose = 2,
                            category = DiscoveryCategory.BAKERY,
                            isEligibleForNgoFree = false,
                            liveTemperature = 25.0,
                            storageType = "ROOM_TEMP",
                            description = "Freshly baked croissant."
                        ),
                        OfferUiModel(
                            id = "2",
                            title = "Sourdough Bread",
                            storeName = "Bakery Delights",
                            imageResId = R.drawable.food_spaghetti,
                            discountPercent = 40,
                            currentPrice = 7.00,
                            originalPrice = 12.00,
                            distanceKm = 0.5,
                            quantityLeft = 3,
                            hoursToClose = 2,
                            category = DiscoveryCategory.BAKERY,
                            isEligibleForNgoFree = false,
                            liveTemperature = 25.0,
                            storageType = "ROOM_TEMP",
                            description = "Artisan sourdough."
                        )
                    )
                )
            ),
            selectedOfferId = "s1",
            onMarkerClick = {},
            onOfferNavigate = {}
        )
    }
}