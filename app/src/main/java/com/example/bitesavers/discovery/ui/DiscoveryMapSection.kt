package com.example.bitesavers.discovery.ui

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bitesavers.discovery.data.NearbyDealMarkerUiModel
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
    modifier: Modifier = Modifier
) {
    val isInPreview = LocalInspectionMode.current

    if (isInPreview) {
        DiscoveryMapPlaceholder(modifier = modifier, markers = markers)
    } else {
        DiscoveryMapMapLibre(modifier = modifier, markers = markers)
    }
}

// For map implementation
@Composable
private fun DiscoveryMapMapLibre(
    markers: List<NearbyDealMarkerUiModel>,
    modifier: Modifier = Modifier
) {
    val styleUrl = "https://api.maptiler.com/maps/streets-v4/style.json?key=3OJ2B5f1qI0Cqbcpt5xf"
    val context = LocalContext.current

    // Initialize the map view exactly once
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context) //fixed command to get the core context and mapview instance
    }

    // Crucial: Tie the map to the app's lifecycle so it actually renders
    val lifecycle = LocalLifecycleOwner.current.lifecycle //grabs the current activity life status
    DisposableEffect(lifecycle, mapView) { //only runs when it appears on screen
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
                //need to declare those events cuz if not, the map will run in the background even
                //if we minimise the app etc, and other cases too
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver) //cleans up observer when composable leaves screen
        }
    }

    //AndroidView = embed traditional Android View
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
        factory = {
            mapView.apply {
                //Tells the parent scroll (LazyColumn) to not steal the drag when user touches the map
                setOnTouchListener { view, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, //fingers touches down
                        MotionEvent.ACTION_MOVE -> { //fingers drag around
                            view.parent.requestDisallowInterceptTouchEvent(true)
                        } //this allows map to take full control
                        MotionEvent.ACTION_UP, //fingers lift up
                        MotionEvent.ACTION_CANCEL -> {
                            view.parent.requestDisallowInterceptTouchEvent(false)
                            //gives control back to the parent scroll
                        }
                    }
                    false
                }

//                Why it's needed: When your app opens, the container (MapView) is created first,
//                but downloading the actual vector map style JSON and tiles from MapTiler happens
//                over the internet asynchronously in the background. If you tried to drop pins or
//                move the camera before the map engine finished booting up, the app would instantly
//                crash with a NullPointerException.

                getMapAsync { map: MapLibreMap -> //only hand the MapLibreMap after the map is fully initialized and ready
                    // Use the Style Builder for safer loading
                    map.setStyle(Style.Builder().fromUri(styleUrl)) { style: Style ->

                        // Center the camera on the first marker
                        val center = markers.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
                            ?: LatLng(3.1390, 101.6869) //calculate the center coordinates

                        // Use moveCamera and CameraUpdateFactory to guarantee it resolves
                        val cameraPosition = CameraPosition.Builder()
                            .target(center)
                            .zoom(14.0)
                            .build()
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                        // Drop the pins
                        markers.forEach { markerData ->
                            map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(markerData.latitude, markerData.longitude))
                                    .title(markerData.labelPrice)
                            ) //loads into the map
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = "Map Preview Placeholder",
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
                    color = MaterialTheme.colorScheme.primary
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
            )
        )
    }
}