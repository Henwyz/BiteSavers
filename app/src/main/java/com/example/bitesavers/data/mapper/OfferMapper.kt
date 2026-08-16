package com.example.bitesavers.data.mapper

import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.remote.dto.OfferDto

fun OfferDto.toUiModel(): OfferUiModel {
    val parsedCategory = try {
        DiscoveryCategory.valueOf(this.category.uppercase())
    } catch (e: Exception) {
        DiscoveryCategory.HOT_MEALS
    }

    // Calculate discount dynamically from prices
    val calculatedDiscount = if (this.originalPrice > 0) {
        (((this.originalPrice - this.currentPrice) / this.originalPrice) * 100).toInt()
    } else {
        0
    }

    return OfferUiModel(
        id = this.id,
        title = this.title,
        storeName = this.storeName,
        imageResId = R.drawable.ic_launcher_foreground,
        discountPercent = calculatedDiscount, // Computed here!
        currentPrice = this.currentPrice,
        originalPrice = this.originalPrice,
        distanceKm = 0.0, // Default 0.0; dynamically calculated via GPS in ViewModel
        quantityLeft = this.quantityLeft,
        hoursToClose = this.hoursToClose,
        pickupWindow = this.pickupWindow ?: "Today, 8:00 PM - 9:30 PM",
        category = parsedCategory,
        isEligibleForNgoFree = this.isEligibleForNgoFree,
        liveTemperature = this.liveTemperature ?: 60.0,
        storageType = this.storageType ?: "HOT",
        description = this.description ?: "Fresh surplus food ready for rescue.",
        latitude = this.latitude,
        longitude = this.longitude
    )
}