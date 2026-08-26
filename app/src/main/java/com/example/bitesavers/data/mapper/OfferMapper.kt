package com.example.bitesavers.data.mapper

import com.example.bitesavers.R
import com.example.bitesavers.customer.discovery.logic.LocationUtils
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.StoreDto

fun OfferDto.toUiModel(
    store: StoreDto? = null,
    userLat: Double? = null,
    userLon: Double? = null
): OfferUiModel {
    val parsedCategory = try {
        this.category?.let { DiscoveryCategory.valueOf(it.uppercase()) } ?: DiscoveryCategory.HOT_MEALS
    } catch (_: Exception) {
        DiscoveryCategory.HOT_MEALS
    }

    val calculatedDiscount = if (this.originalPrice > 0 && this.discountedPrice < this.originalPrice) {
        (((this.originalPrice - this.discountedPrice) / this.originalPrice) * 100).toInt()
    } else {
        0
    }

    val window = if (store?.openingTime != null && store.closingTime != null) {
        "${store.openingTime.take(5)} - ${store.closingTime.take(5)}"
    } else {
        "Today • 6:00 PM - 9:00 PM"
    }

    // Calculate real distance if both user and store coordinates exist
    val computedDistance = if (userLat != null && userLon != null && store?.latitude != null && store.longitude != null) {
        LocationUtils.calculateDistanceKm(
            lat1 = userLat,
            lon1 = userLon,
            lat2 = store.latitude,
            lon2 = store.longitude
        )
    } else {
        0.8 // default fallback
    }

    return OfferUiModel(
        id = this.id,
        title = this.title,
        storeName = store?.name ?: "BiteSavers Partner Store",
        imageResId = R.drawable.ic_launcher_foreground,
        imageUrl = this.imageUrl ?: store?.imageUrl,
        discountPercent = calculatedDiscount,
        currentPrice = this.discountedPrice,
        originalPrice = this.originalPrice,
        distanceKm = computedDistance,
        quantityLeft = this.quantityAvailable,
        hoursToClose = 2,
        pickupWindow = window,
        category = parsedCategory,
        isEligibleForNgoFree = this.isEligibleForNgoFree,
        liveTemperature = 60.0,
        storageType = if (parsedCategory == DiscoveryCategory.HOT_MEALS) "HOT" else "AMBIENT",
        description = this.description ?: "Fresh surplus food ready for rescue.",
        latitude = store?.latitude,
        longitude = store?.longitude
    )
}