package com.example.bitesavers.data.model

data class OfferUiModel(
    val id: String,
    val title: String,
    val storeName: String,
    val imageResId: Int,
    val discountPercent: Int,
    val currentPrice: Double,
    val originalPrice: Double,
    val distanceKm: Double,
    val quantityLeft: Int,
    val hoursToClose: Int,
    val category: DiscoveryCategory,
    val isEligibleForNgoFree: Boolean,
    val liveTemperature: Double = 60.0,
    val storageType: String = "HOT",
    // NEW: Dynamic description from the business
    val description: String
)
