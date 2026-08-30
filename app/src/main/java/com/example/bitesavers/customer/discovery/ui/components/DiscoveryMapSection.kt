package com.example.bitesavers.customer.discovery.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
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
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.engine.LocationEngineRequest
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
    onLocationResolved: (Double, Double) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val isInPreview = LocalInspectionMode.current
    var isExpanded by rememberSaveable { mutableStateOf(false) }

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
                onMarkerClick = onMarkerClick,
                onLocationResolved = onLocationResolved
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
    onLocationResolved: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val styleUrl = "https://api.maptiler.com/maps/streets-v4/style.json?key=3OJ2B5f1qI0Cqbcpt5xf"
    val context = LocalContext.current

    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var hasCenteredInitially by rememberSaveable { mutableStateOf(false) }

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

    // Automatically centers camera once on valid GPS lock without re-triggering during scroll
    LaunchedEffect(userLatitude, userLongitude, mapInstance) {
        val map = mapInstance
        if (map != null && userLatitude != null && userLongitude != null && !hasCenteredInitially) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(userLatitude, userLongitude),
                    15.5
                ),
                600
            )
            hasCenteredInitially = true
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
                                val locationComponentOptions = LocationComponentOptions.builder(context)
                                    .pulseEnabled(true)
                                    .build()

                                val request = LocationEngineRequest.Builder(1000)
                                    .setFastestInterval(1000)
                                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                                    .build()

                                val activationOptions = LocationComponentActivationOptions
                                    .builder(context, style)
                                    .locationComponentOptions(locationComponentOptions)
                                    .useDefaultLocationEngine(true)
                                    .locationEngineRequest(request)
                                    .build()

                                val locationComponent = map.locationComponent
                                locationComponent.activateLocationComponent(activationOptions)
                                locationComponent.isLocationComponentEnabled = true
                                locationComponent.cameraMode = CameraMode.TRACKING
                                locationComponent.renderMode = RenderMode.COMPASS
                                locationComponent.zoomWhileTracking(15.5)

                                // Center map on initial load and notify caller of current GPS coordinates
                                val lastLoc = locationComponent.lastKnownLocation
                                if (lastLoc != null) {
                                    onLocationResolved(lastLoc.latitude, lastLoc.longitude)
                                    if (!hasCenteredInitially) {
                                        map.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(lastLoc.latitude, lastLoc.longitude),
                                                15.5
                                            )
                                        )
                                        hasCenteredInitially = true
                                    }
                                } else if (userLatitude != null && userLongitude != null) {
                                    if (!hasCenteredInitially) {
                                        map.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(userLatitude, userLongitude),
                                                15.5
                                            )
                                        )
                                        hasCenteredInitially = true
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            // Draws markers when the style finishes loading on initial startup
                            val iconFactory = IconFactory.getInstance(context)
                            val markerIcon = iconFactory.defaultMarker()
                            markers.forEach { markerData ->
                                map.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(markerData.latitude, markerData.longitude))
                                        .title(markerData.labelText)
                                        .snippet(markerData.storeId)
                                        .icon(markerIcon)
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
                    // Ensures markers refresh smoothly when ViewModel state updates
                    if (map.style != null) {
                        map.clear()
                        val iconFactory = IconFactory.getInstance(context)
                        val markerIcon = iconFactory.defaultMarker()

                        markers.forEach { markerData ->
                            map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(markerData.latitude, markerData.longitude))
                                    .title(markerData.labelText)
                                    .snippet(markerData.storeId)
                                    .icon(markerIcon)
                            )
                        }
                    }
                }
            }
        )

        // Floating Action Controls: Recenter & Expand Buttons with comfortable spacing
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recenter to user's live position
            IconButton(
                onClick = {
                    val map = mapInstance
                    if (map != null) {
                        try {
                            // 1. Try MapLibre LocationComponent
                            val locComponent = map.locationComponent
                            if (locComponent.isLocationComponentActivated) {
                                locComponent.cameraMode = CameraMode.TRACKING
                                val lastKnown = locComponent.lastKnownLocation
                                if (lastKnown != null) {
                                    onLocationResolved(lastKnown.latitude, lastKnown.longitude)
                                    map.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(lastKnown.latitude, lastKnown.longitude),
                                            15.5
                                        ),
                                        600
                                    )
                                    return@IconButton
                                }
                            }

                            // 2. Direct Android LocationManager fallback
                            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                            val gpsLocation: Location? = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                            if (gpsLocation != null) {
                                onLocationResolved(gpsLocation.latitude, gpsLocation.longitude)
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(gpsLocation.latitude, gpsLocation.longitude),
                                        15.5
                                    ),
                                    600
                                )
                                return@IconButton
                            }

                            // 3. ViewModel Coordinates fallback
                            if (userLatitude != null && userLongitude != null) {
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(userLatitude, userLongitude),
                                        15.5
                                    ),
                                    600
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_my_location),
                    contentDescription = stringResource(id = R.string.cd_recenter_location),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expand / Collapse map toggle
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isExpanded) R.drawable.ic_fullscreen_exit else R.drawable.ic_fullscreen
                    ),
                    contentDescription = stringResource(id = R.string.cd_toggle_fullscreen),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
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

@Preview(showBackground = true, name = "Discovery Map - Default")
@Composable
private fun DiscoveryMapSectionPreview() {
    BiteSaversTheme {
        DiscoveryMapSection(
            markers = listOf(
                NearbyDealMarkerUiModel(
                    storeId = "s1",
                    storeName = "Raja Uda Aroma Bakery",
                    labelText = "2 DEALS",
                    latitude = 5.4325,
                    longitude = 100.3855,
                    offers = listOf(
                        OfferUiModel(
                            id = "1",
                            title = "Golden Egg Tart Box (4 pcs)",
                            storeName = "Raja Uda Aroma Bakery",
                            imageResId = R.drawable.food_spaghetti,
                            discountPercent = 50,
                            currentPrice = 8.00,
                            originalPrice = 16.00,
                            distanceKm = 0.4,
                            quantityLeft = 5,
                            hoursToClose = 2,
                            category = DiscoveryCategory.BAKERY,
                            isEligibleForNgoFree = true,
                            liveTemperature = 25.0,
                            storageType = "ROOM_TEMP",
                            description = "Freshly baked egg tarts."
                        )
                    )
                )
            ),
            selectedOfferId = null,
            onMarkerClick = {},
            onOfferNavigate = {}
        )
    }
}

@Preview(showBackground = true, name = "Discovery Map - Selected Store Carousel")
@Composable
private fun DiscoveryMapSectionSelectedPreview() {
    BiteSaversTheme {
        DiscoveryMapSection(
            markers = listOf(
                NearbyDealMarkerUiModel(
                    storeId = "s1",
                    storeName = "Raja Uda Aroma Bakery",
                    labelText = "2 DEALS",
                    latitude = 5.4325,
                    longitude = 100.3855,
                    offers = listOf(
                        OfferUiModel(
                            id = "1",
                            title = "Golden Egg Tart Box (4 pcs)",
                            storeName = "Raja Uda Aroma Bakery",
                            imageResId = R.drawable.food_spaghetti,
                            discountPercent = 50,
                            currentPrice = 8.00,
                            originalPrice = 16.00,
                            distanceKm = 0.4,
                            quantityLeft = 5,
                            hoursToClose = 2,
                            category = DiscoveryCategory.BAKERY,
                            isEligibleForNgoFree = true,
                            liveTemperature = 25.0,
                            storageType = "ROOM_TEMP",
                            description = "Freshly baked egg tarts."
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