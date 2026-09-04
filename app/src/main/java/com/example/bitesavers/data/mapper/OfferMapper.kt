package com.example.bitesavers.data.mapper

import com.example.bitesavers.R
import com.example.bitesavers.data.dto.StorageBoxDto
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.StoreDto
import com.example.bitesavers.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Set to true to bypass operating hours during development so all stores/offers appear open
const val DEBUG_BYPASS_OPERATING_HOURS = false

fun OfferDto.toUiModel(
    store: StoreDto?,
    storageBox: StorageBoxDto? = null
): OfferUiModel {
    val origPrice = originalPrice ?: 0.0
    val discPrice = discountedPrice ?: 0.0
    val calculatedDiscount = if (origPrice > 0) {
        (((origPrice - discPrice) / origPrice) * 100).toInt()
    } else 0

    // Visual 6% tax calculation applied across all UI models so card, header, and detail prices tally
    val visualCurrentPrice = if (discPrice > 0.0) Math.round(discPrice * 1.06 * 100.0) / 100.0 else 0.0
    val visualOriginalPrice = if (origPrice > 0.0) Math.round(origPrice * 1.06 * 100.0) / 100.0 else 0.0

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

    // IoT storage classification and live temperature reading from connected storage box
    val isHotHolding = storageBox?.storageType?.contains("Hot", ignoreCase = true) == true
    val resolvedStorageType = if (isHotHolding) "HOT" else "COLD"
    val resolvedTemperature = storageBox?.currentTemperature ?: if (isHotHolding) 60.0 else 4.0

    // Evaluates dynamic NGO free claim windows across 1-hour post-pickup and store cleaning periods
    val dynamicNgoEligible = evaluateNgoFreeEligibility(
        pickupStartStr = this.pickupStart,
        pickupEndStr = this.pickupEnd,
        storeClosingStr = store?.closingTime,
        storeCleaningStr = store?.cleanupEndTime,
        persistedFlag = this.isEligibleForNgoFree
    )

    return OfferUiModel(
        id = this.id,
        storeId = this.storeId ?: store?.id ?: "store_01", // Maps foreign key store ID from Supabase
        title = this.title,
        storeName = store?.name ?: "Store",
        storeRating = store?.rating ?: 4.8,
        imageUrl = this.imageUrl,
        discountPercent = calculatedDiscount,
        currentPrice = visualCurrentPrice,
        originalPrice = visualOriginalPrice,
        distanceKm = 0.0,
        quantityLeft = this.quantityAvailable ?: 0,
        hoursToClose = remainingHours,
        pickupWindow = formattedPickupWindow,
        category = mappedCategory,
        isEligibleForNgoFree = dynamicNgoEligible,
        liveTemperature = resolvedTemperature,
        storageType = resolvedStorageType,
        description = this.description ?: "Fresh surplus food ready for rescue.",
        latitude = store?.latitude,
        longitude = store?.longitude,
        timeStatusText = timeStatus,
        isCurrentlyOpen = isOpenNow
    )
}

/**
 * Evaluates NGO Free Eligibility across two operational scenarios in Malaysia Standard Time:
 * 1. Afternoon window: for 1 hour immediately following the merchant's custom pickup_end (and before store closing hour).
 * 2. Closing and Cleaning window: active from store closing time until store cleaning ends (e.g. 5:00 PM to 10:00 PM).
 */
private fun evaluateNgoFreeEligibility(
    pickupStartStr: String?,
    pickupEndStr: String?,
    storeClosingStr: String?,
    storeCleaningStr: String?,
    persistedFlag: Boolean
): Boolean {
    if (persistedFlag) return true

    val currentMinutes = TimeUtils.getCurrentMinutesOfDay()
    val pickupEndMinutes = parseTimeToMinutes(pickupEndStr)
    val storeClosingMinutes = parseTimeToMinutes(storeClosingStr)
    val storeCleaningMinutes = parseTimeToMinutes(storeCleaningStr)
        ?: (storeClosingMinutes?.plus(60) ?: ((pickupEndMinutes ?: 0) + 60))

    // Scenario 2: Strictly active ONLY between store close and cleanup end time (e.g. 5:00 PM to 10:00 PM)
    val isCleaningPeriod = storeClosingMinutes != null &&
            currentMinutes >= storeClosingMinutes &&
            currentMinutes <= storeCleaningMinutes

    // Scenario 1: Afternoon window — free for 1 hour immediately after regular pickup ends, before store closing
    val isPostPickupPeriod = if (pickupEndMinutes != null) {
        val oneHourAfterPickupEnd = pickupEndMinutes + 60
        val isBeforeStoreClose = storeClosingMinutes == null || currentMinutes < storeClosingMinutes
        currentMinutes in pickupEndMinutes..oneHourAfterPickupEnd && isBeforeStoreClose
    } else false

    return isCleaningPeriod || isPostPickupPeriod
}

// Evaluates current system time against store opening and closing intervals to derive dynamic status text
private fun calculateTimeStatus(
    openingTimeString: String?,
    closingTimeString: String?
): Triple<String, Boolean, Int> {
    if (DEBUG_BYPASS_OPERATING_HOURS) {
        return Triple("Closes in 2h", true, 2)
    }

    val currentMinutes = TimeUtils.getCurrentMinutesOfDay()
    val openMinutes = parseTimeToMinutes(openingTimeString)
    val closeMinutes = parseTimeToMinutes(closingTimeString)

    if (openMinutes == null && closeMinutes == null) {
        return Triple("Closes in 2h", true, 2)
    }

    if (openMinutes != null && currentMinutes < openMinutes) {
        val diffMinutes = openMinutes - currentMinutes
        val hours = (diffMinutes / 60).coerceAtLeast(1)
        return Triple("Opens in ${hours}h", false, 0)
    }

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
// Parses both 12-hour AM/PM ("02:30:00 PM") and 24-hour ("14:30:00") time strings into minutes from midnight
private fun parseTimeToMinutes(timeString: String?): Int? {
    if (timeString.isNullOrBlank()) return null
    val raw = timeString.trim().uppercase()
    return try {
        val isPm = raw.contains("PM")
        val isAm = raw.contains("AM")
        val clean = raw.replace("AM", "").replace("PM", "").trim()
        val parts = clean.split(":")
        var hour = parts[0].trim().toInt()
        val minute = if (parts.size > 1) parts[1].trim().toInt() else 0

        if (isPm && hour < 12) hour += 12
        if (isAm && hour == 12) hour = 0

        (hour * 60) + minute
    } catch (_: Exception) {
        null
    }
}

// Converts 24-hour SQL format (e.g., 20:30:00) safely to 12-hour AM/PM format (API 24 compatible)
private fun formatTimeToAmPm(timeStr: String?): String? {
    if (timeStr.isNullOrBlank()) return null
    return try {
        val parser = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        }
        val date = parser.parse(timeStr.trim().take(5)) ?: return timeStr
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        }
        formatter.format(date)
    } catch (_: Exception) {
        timeStr
    }
}