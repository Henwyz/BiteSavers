package com.example.bitesavers.customer.discovery.logic

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {

    /**
     * Calculates the great-circle distance (in kilometers) between two GPS points
     * using the Haversine formula.
     */
    fun calculateDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    /**
     * Safely reads the device's last known GPS/Network location coordinates.
     */
    @SuppressLint("MissingPermission")
    fun fetchDeviceCoordinates(
        context: Context,
        onCoordinatesFetched: (latitude: Double, longitude: Double) -> Unit
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return

        val gpsLocation: Location? = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            null
        }

        val networkLocation: Location? = try {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            null
        }

        val bestLocation = gpsLocation ?: networkLocation
        if (bestLocation != null) {
            onCoordinatesFetched(bestLocation.latitude, bestLocation.longitude)
        } else {
            // Default fallback coordinates (e.g. Kuala Lumpur)
            onCoordinatesFetched(3.1390, 101.6869)
        }
    }
}