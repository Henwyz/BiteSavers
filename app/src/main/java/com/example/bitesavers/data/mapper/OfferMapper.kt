package com.example.bitesavers.data.mapper

import com.example.bitesavers.R
import com.example.bitesavers.data.dto.StorageBoxDto
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.StoreDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Set to true to bypass opening hours during development so all stores/offers appear open
const val DEBUG_BYPASS_OPERATING_HOURS = true

fun OfferDto.toUiModel(
    store: StoreDto?,
    storageBox: StorageBoxDto? = null
): OfferUiModel {
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

    // Dynamic calculation of hours left until surplus pickup closes (prioritizing pickup_end over closing_time)
    val targetEndTime = this.pickupEnd ?: store?.closingTime
    val openingTime = store?.openingTime

    val (timeStatus, isOpenNow, remainingHours) = calculateTimeStatus(
        openingTimeString = openingTime,
        closingTimeString = targetEndTime
    )

    // Formats pickup interval into readable display string
    val formattedStart = formatTimeToAmPm(this.pickupStart) ?: "8:00 PM"
    val formattedEnd = formatTimeToAmPm(this.pickupEnd) ?: "10:00 PM"
    val formattedPickupWindow = "Today, $formattedStart - $formattedEnd"

    // Selects a category-appropriate local fallback drawable when imageUrl is unavailable
    val fallbackImageResId = when (mappedCategory) {
        DiscoveryCategory.BAKERY -> R.drawable.ic_launcher_foreground
        DiscoveryCategory.HOT_MEALS -> R.drawable.ic_launcher_foreground
        DiscoveryCategory.DESSERTS -> R.drawable.ic_launcher_foreground
        DiscoveryCategory.BEVERAGES -> R.drawable.ic_launcher_foreground
        else -> R.drawable.ic_launcher_foreground
    }

    // Resolves IoT storage classification and live temperature reading from connected storage box
    val isHotHolding = storageBox?.storageType?.contains("Hot", ignoreCase = true) == true
    val resolvedStorageType = if (isHotHolding) "HOT" else "COLD"
    val resolvedTemperature = storageBox?.currentTemperature ?: if (isHotHolding) 60.0 else 4.0

    return OfferUiModel(
        id = this.id,
        storeId = this.storeId ?: store?.id ?: "store_01", // Maps foreign key store ID from Supabase
        title = this.title,
        storeName = store?.name ?: "Store",
        storeRating = store?.rating ?: 4.8,
        imageUrl = this.imageUrl,
        discountPercent = calculatedDiscount,
        currentPrice = discPrice,
        originalPrice = origPrice,
        distanceKm = 0.0,
        quantityLeft = this.quantityAvailable ?: 0,
        hoursToClose = remainingHours,
        pickupWindow = formattedPickupWindow,
        category = mappedCategory,
        isEligibleForNgoFree = this.isEligibleForNgoFree,
        liveTemperature = resolvedTemperature,
        storageType = resolvedStorageType,
        description = this.description ?: "Fresh surplus food ready for rescue.",
        latitude = store?.latitude,
        longitude = store?.longitude,
        timeStatusText = timeStatus,
        isCurrentlyOpen = isOpenNow
    )
}

// Evaluates current system time against store opening and closing intervals to derive dynamic status text
private fun calculateTimeStatus(
    openingTimeString: String?,
    closingTimeString: String?
): Triple<String, Boolean, Int> {
    // If debug bypass is enabled, treat all stores as open and closing in 2 hours
    if (DEBUG_BYPASS_OPERATING_HOURS) {
        return Triple("Closes in 2h", true, 2)
    }

    val now = Calendar.getInstance()
    val currentMinutes = (now.get(Calendar.HOUR_OF_DAY) * 60) + now.get(Calendar.MINUTE)

    val openMinutes = parseTimeToMinutes(openingTimeString)
    val closeMinutes = parseTimeToMinutes(closingTimeString)

    // Default fallback when time records are unspecified
    if (openMinutes == null && closeMinutes == null) {
        return Triple("Closes in 2h", true, 2)
    }

    // Handles store opening anticipation if current time is prior to opening hours
    if (openMinutes != null && currentMinutes < openMinutes) {
        val diffMinutes = openMinutes - currentMinutes
        val hours = (diffMinutes / 60).coerceAtLeast(1)
        return Triple("Opens in ${hours}h", false, 0)
    }

    // Handles active operating window
    if (closeMinutes != null) {
        if (currentMinutes <= closeMinutes) {
            val diffMinutes = closeMinutes - currentMinutes
            val hours = (diffMinutes / 60).coerceAtLeast(1)
            return Triple("Closes in ${hours}h", true, hours)
        } else {
            return Triple("Closed", false, 0)
        }
    }

    return Triple("Closes in 2h", true, 2)
}

// Parses SQL 24-hour time strings safely into total minutes since midnight
private fun parseTimeToMinutes(timeString: String?): Int? {
    if (timeString.isNullOrBlank()) return null
    return try {
        val parts = timeString.split(":")
        val hour = parts[0].trim().toInt()
        val minute = if (parts.size > 1) parts[1].trim().take(2).toInt() else 0
        (hour * 60) + minute
    } catch (_: Exception) {
        null
    }
}

// Converts 24-hour SQL format (e.g., 20:30:00) safely to 12-hour AM/PM format (API 24 compatible)
private fun formatTimeToAmPm(timeStr: String?): String? {
    if (timeStr.isNullOrBlank()) return null
    return try {
        val parser = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = parser.parse(timeStr.trim().take(5)) ?: return timeStr
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        formatter.format(date)
    } catch (_: Exception) {
        timeStr
    }
}