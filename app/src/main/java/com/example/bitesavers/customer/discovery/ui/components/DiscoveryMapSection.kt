package com.example.bitesavers.customer.discovery.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bitesavers.R
import com.example.bitesavers.customer.discovery.data.NearbyDealMarkerUiModel
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
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory

@Composable
fun DiscoveryMapSection(
    markers: List<NearbyDealMarkerUiModel>,
    offers: List<OfferUiModel> = emptyList(),
    userRole: UserRole = UserRole.CONSUMER,
    selectedOfferId: String?,
    onMarkerClick: (String?) -> Unit,
    onOfferNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isInPreview = LocalInspectionMode.current //For preview ppurpose only

    // The Box allows the Compose overlay card to float on top of the MapLibre AndroidView
    Box(modifier = modifier) {
        if (isInPreview) {
            DiscoveryMapPlaceholder(
                markers = markers,
                onMarkerClick = onMarkerClick
            )
        } else {
            DiscoveryMapMapLibre(
                markers = markers,
                onMarkerClick = onMarkerClick
            )
        }

        // --- THE POPUP OVERLAY CARD ---
        if (selectedOfferId != null) {
            val selectedOffer = offers.find { it.id == selectedOfferId }

            if (selectedOffer != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .widthIn(max = 280.dp) // Limits the card width so it doesn't span the whole map width
                ) {
                    CompactDiscoveryOfferCard(
                        offer = selectedOffer,
                        userRole = userRole,
                        onClick = { onOfferNavigate(selectedOffer.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoveryMapMapLibre(
    markers: List<NearbyDealMarkerUiModel>,
    onMarkerClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val styleUrl = "https://api.maptiler.com/maps/streets-v4/style.json?key=3OJ2B5f1qI0Cqbcpt5xf"
    val context = LocalContext.current

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

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp) // Made it slightly taller so the popup fits nicely
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
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
                    map.setStyle(Style.Builder().fromUri(styleUrl)) { style: Style ->

                        val center = markers.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
                            ?: LatLng(3.1390, 101.6869)

                        val cameraPosition = CameraPosition.Builder()
                            .target(center)
                            .zoom(14.0)
                            .build()
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                        // Add markers and hide the ID inside the "snippet"
                        markers.forEach { markerData ->
                            map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(markerData.latitude, markerData.longitude))
                                    .title(markerData.labelPrice)
                                    .snippet(markerData.id) // Hide the ID here!
                            )
                        }

                        // Listen for marker clicks
                        map.setOnMarkerClickListener { marker ->
                            val clickedId = marker.snippet
                            if (clickedId != null) {
                                // Smoothly animate the camera to center on the clicked pin
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        marker.position,
                                        15.0 // Keep a focused zoom level
                                    ),
                                    400 // Animation duration in milliseconds
                                )
                                onMarkerClick(clickedId)
                                true
                            } else {
                                false
                            }
                        }
                        // Dismiss the card if they click the empty map background
                        map.addOnMapClickListener {
                            onMarkerClick(null)
                            false
                        }
                    }
                }
            }
        }
    )
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
            .height(190.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
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
                    text = it.labelPrice,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onMarkerClick(it.id) }
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
                NearbyDealMarkerUiModel("1", "RM 9", 3.1466, 101.6958),
                NearbyDealMarkerUiModel("2", "RM 12", 3.1579, 101.7115),
                NearbyDealMarkerUiModel("3", "RM 7", 3.1319, 101.6841)
            ),
            selectedOfferId = "1", // Hardcoded to show the popup in preview
            onMarkerClick = {},
            onOfferNavigate = {}
        )
    }
}