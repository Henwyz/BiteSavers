package com.example.bitesavers.data.mapper

import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.StoreDto

fun OfferDto.toUiModel(store: StoreDto?): OfferUiModel {
    val origPrice = originalPrice ?: 0.0
    val discPrice = discountedPrice ?: 0.0
    val calculatedDiscount = if (origPrice > 0) {
        (((origPrice - discPrice) / origPrice) * 100).toInt()
    } else 0

    val mappedCategory = when (category?.uppercase()) {
        "BAKERY" -> DiscoveryCategory.BAKERY
        "HOT_MEALS" -> DiscoveryCategory.HOT_MEALS
        "DESSERTS" -> DiscoveryCategory.DESSERTS
        "BEVERAGES" -> DiscoveryCategory.BEVERAGES
        else -> DiscoveryCategory.ALL
    }

    return OfferUiModel(
        id = id,
        title = title,
        storeName = store?.name ?: "Local Merchant",
        storeRating = store?.rating ?: 4.8,
        imageResId = R.drawable.ic_launcher_foreground,
        imageUrl = imageUrl,
        discountPercent = calculatedDiscount,
        currentPrice = discPrice,
        originalPrice = origPrice,
        quantityLeft = quantityAvailable ?: 0,
        hoursToClose = 2,
        category = mappedCategory,
        isEligibleForNgoFree = isEligibleForNgoFree ?: false,
        liveTemperature = 25.0,
        storageType = "ROOM_TEMP",
        description = description ?: "Fresh surplus food ready for rescue.",
        latitude = store?.latitude,
        longitude = store?.longitude
    )
}