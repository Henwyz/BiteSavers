package com.example.bitesavers.data.model

import androidx.annotation.DrawableRes
import com.example.bitesavers.R

data class OfferUiModel(
    val id: String,
    val storeId: String = "",
    val title: String,
    val storeName: String,
    val storeRating: Double? = 4.8,
    @DrawableRes val imageResId: Int = R.drawable.ic_launcher_foreground,
    val imageUrl: String? = null,
    val discountPercent: Int = 0,
    val currentPrice: Double,
    val originalPrice: Double,
    val distanceKm: Double = 0.0,
    val quantityLeft: Int,
    val hoursToClose: Int,
    val pickupWindow: String = "Today, 8:00 PM - 9:30 PM",
    val category: DiscoveryCategory,
    val isEligibleForNgoFree: Boolean = false,
    val liveTemperature: Double = 60.0,
    val storageType: String = "HOT",
    val description: String = "Fresh surplus food ready for rescue.",
    val latitude: Double? = null,
    val longitude: Double? = null
)