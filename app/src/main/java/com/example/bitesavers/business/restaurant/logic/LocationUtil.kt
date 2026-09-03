package com.example.bitesavers.util

import android.content.Context
import android.location.Geocoder
import java.util.Locale

object LocationUtils {

    // Default fallback coordinates (Penang, Malaysia) if address lookup fails
    private const val DEFAULT_LAT = 5.4674
    private const val DEFAULT_LNG = 100.2790

    fun getCoordinatesFromAddress(context: Context, address: String): Pair<Double, Double> {
        if (address.isBlank()) {
            return Pair(DEFAULT_LAT, DEFAULT_LNG)
        }

        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addressList = geocoder.getFromLocationName(address, 1)

            if (!addressList.isNullOrEmpty()) {
                val match = addressList[0]
                Pair(match.latitude, match.longitude)
            } else {
                Pair(DEFAULT_LAT, DEFAULT_LNG)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(DEFAULT_LAT, DEFAULT_LNG)
        }
    }
}