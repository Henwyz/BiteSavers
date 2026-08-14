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

    // "Attach this new function named toUiModel() directly onto any OfferDto object."
    return OfferUiModel(
        id = this.id,
        title = this.title,
        storeName = this.storeName,
        // Default placeholder image resource; replace with dynamic image loader or resource if needed
        imageResId = R.drawable.ic_launcher_foreground,
        discountPercent = this.discountPercent,
        currentPrice = this.currentPrice,
        originalPrice = this.originalPrice,
        distanceKm = this.distanceKm,
        quantityLeft = this.quantityLeft,
        hoursToClose = this.hoursToClose,
        category = parsedCategory,
        isEligibleForNgoFree = this.isEligibleForNgoFree,
        liveTemperature = this.liveTemperature ?: 60.0,
        storageType = this.storageType ?: "HOT",
        description = this.description ?: "Fresh surplus food ready for rescue."
    )
}