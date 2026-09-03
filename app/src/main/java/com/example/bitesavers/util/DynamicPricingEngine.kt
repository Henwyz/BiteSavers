package com.example.bitesavers.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

object DynamicPricingEngine {

    /**
     * Calculates initial suggested discount when creating or editing a listing.
     * Uses the duration and window position so newly created listings start with a valid consumer discount.
     */
    fun calculateSuggestedPrice(
        originalPrice: Double,
        pickupEndTimeStr: String,
        pickupStartTimeStr: String = ""
    ): Pair<Double, Int> {
        if (originalPrice <= 0.0) return Pair(0.0, 0)

        val minutesRemaining = getMinutesUntil(pickupEndTimeStr, pickupStartTimeStr)

        val discountRate = when {
            // Less than 45 mins before pickup ends: 70% clearance
            minutesRemaining in 1..45 -> 0.70
            // 45 to 120 mins: 50%
            minutesRemaining in 46..120 -> 0.50
            // 2 to 4 hours: 40%
            minutesRemaining in 121..240 -> 0.40
            // More than 4 hours: standard early surplus discount 35%
            minutesRemaining > 240 -> 0.35
            // Pickup window ended: NGO claim window
            else -> 0.50 // Fallback for publishing fresh listings
        }

        val calculated = originalPrice * (1.0 - discountRate)
        val roundedPrice = (calculated * 10.0).roundToInt() / 10.0
        val percent = (discountRate * 100).toInt()

        return Pair(roundedPrice.coerceAtLeast(1.0), percent)
    }

    /**
     * Recommends pickup start and end times based on store opening, closing, and current time.
     */
    /**
     * Recommends pickup start and end times based on store hours.
     * Always anchors to store closing time: [closing - 2 hours] to [closing].
     * Falls back to current time + 2h only if store closing time is missing.
     */
    fun generateDefaultPickupWindow(
        openingTime24: String?,
        closingTime24: String?
    ): Pair<String, String> {
        val parser = SimpleDateFormat("HH:mm", Locale.US)
        val formatter12 = SimpleDateFormat("hh:mm a", Locale.US)

        val closingDate = try {
            closingTime24?.let { parser.parse(it.take(5)) }
        } catch (_: Exception) {
            null
        }

        // If the store has a valid closing time in database, always anchor to it
        if (closingDate != null) {
            val endCal = Calendar.getInstance().apply { time = closingDate }
            val startCal = Calendar.getInstance().apply {
                time = closingDate
                add(Calendar.HOUR_OF_DAY, -2)
            }
            return Pair(formatter12.format(startCal.time), formatter12.format(endCal.time))
        }

        // Fallback only if store closing time is null or unparseable
        val start = Calendar.getInstance().apply { add(Calendar.MINUTE, 30) }
        val end = Calendar.getInstance().apply {
            time = start.time
            add(Calendar.HOUR_OF_DAY, 2)
        }

        return Pair(formatter12.format(start.time), formatter12.format(end.time))
    }

    /**
     * Converts a 12-hour string (e.g. "05:00 PM") to Postgres 24-hour format ("17:00:00").
     */
    fun to24HourTime(time12: String?): String? {
        if (time12.isNullOrBlank()) return null
        return try {
            val parser = SimpleDateFormat("hh:mm a", Locale.US)
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.US)
            val date = parser.parse(time12.trim())
            if (date != null) formatter.format(date) else time12
        } catch (_: Exception) {
            time12
        }
    }

    private fun getMinutesUntil(time12Str: String, start12Str: String = ""): Long {
        return try {
            val parser = SimpleDateFormat("hh:mm a", Locale.US)
            val targetTime = parser.parse(time12Str.trim()) ?: return 120

            val now = Calendar.getInstance()
            val targetCal = Calendar.getInstance().apply {
                time = targetTime
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            }

            var diff = (targetCal.timeInMillis - now.timeInMillis) / (60 * 1000)

            // If time already passed today (e.g., testing at 11 PM for an 8 PM window),
            // calculate the window duration between start and end instead of returning negative
            if (diff <= 0 && start12Str.isNotBlank()) {
                val startTime = parser.parse(start12Str.trim())
                if (startTime != null) {
                    val duration = (targetTime.time - startTime.time) / (60 * 1000)
                    if (duration > 0) return duration
                }
                return 120 // 2 hours default fallback
            }

            diff
        } catch (_: Exception) {
            120
        }
    }
}