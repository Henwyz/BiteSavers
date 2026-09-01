package com.example.bitesavers.data.mapper

import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.StoreDto
import java.util.Calendar

fun OfferDto.toUiModel(store: StoreDto?): OfferUiModel {
    val origPrice = originalPrice ?: 0.0
    val discPrice = discountedPrice ?: 0.0
    val calculatedDiscount = if (origPrice > 0) {
        (((origPrice - discPrice) / origPrice) * 100).toInt()
    } else 0

    // Normalizes input strings by converting spaces and dashes to underscores so "Hot Meals" matches HOT_MEALS
    val normalizedCategory = category
        ?.trim()
        ?.uppercase()
        ?.replace(" ", "_")
        ?.replace("-", "_")

    val mappedCategory = when (normalizedCategory) {
        "BAKERY" -> DiscoveryCategory.BAKERY
        "HOT_MEALS" -> DiscoveryCategory.HOT_MEALS
        "DESSERTS" -> DiscoveryCategory.DESSERTS
        "BEVERAGES" -> DiscoveryCategory.BEVERAGES
        else -> DiscoveryCategory.ALL
    }

    // Dynamic calculation of hours left until store closing time
    val calculatedHoursToClose = calculateHoursRemaining(store?.closingTime)

    // Selects a category-appropriate local fallback drawable when imageUrl is unavailable
    val fallbackImageResId = when (mappedCategory) {
        DiscoveryCategory.BAKERY -> R.drawable.food_spaghetti // Replace with R.drawable.food_croissant / bakery drawable if available
        DiscoveryCategory.HOT_MEALS -> R.drawable.food_spaghetti
        DiscoveryCategory.DESSERTS -> R.drawable.food_spaghetti // Replace with dessert drawable if available
        DiscoveryCategory.BEVERAGES -> R.drawable.food_spaghetti // Replace with drink drawable if available
        else -> R.drawable.food_spaghetti
    }

    return OfferUiModel(
        id = id,
        title = title,
        storeName = store?.name ?: "Local Merchant",
        storeRating = store?.rating ?: 4.8,
        imageResId = fallbackImageResId,
        imageUrl = imageUrl,
        discountPercent = calculatedDiscount,
        currentPrice = discPrice,
        originalPrice = origPrice,
        quantityLeft = quantityAvailable ?: 0,
        hoursToClose = calculatedHoursToClose,
        category = mappedCategory,
        isEligibleForNgoFree = isEligibleForNgoFree ?: false,
        liveTemperature = 25.0,
        storageType = "ROOM_TEMP",
        description = description ?: "Fresh surplus food ready for rescue.",
        latitude = store?.latitude,
        longitude = store?.longitude
    )
}

// Computes remaining hours until store closes, accounting for regular operating hours and midnight rollovers
private fun calculateHoursRemaining(closingTimeString: String?): Int {
    if (closingTimeString.isNullOrBlank()) return 2

    return try {
        val currentCalendar = Calendar.getInstance()
        val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentCalendar.get(Calendar.MINUTE)

        val closingParts = closingTimeString.split(":")
        val closingHour = closingParts[0].trim().toInt()
        val closingMinute = if (closingParts.size > 1) closingParts[1].trim().toInt() else 0

        val currentTotalMinutes = (currentHour * 60) + currentMinute
        var closingTotalMinutes = (closingHour * 60) + closingMinute

        // Accounts for stores with operating hours extending past midnight into early morning
        if (closingTotalMinutes < currentTotalMinutes) {
            closingTotalMinutes += 24 * 60
        }

        val diffMinutes = closingTotalMinutes - currentTotalMinutes
        if (diffMinutes <= 0) 1 else (diffMinutes / 60).coerceAtLeast(1)
    } catch (e: Exception) {
        2
    }
}