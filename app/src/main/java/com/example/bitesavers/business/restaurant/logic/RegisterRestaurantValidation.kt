package com.example.bitesavers.business.restaurant.logic

import java.text.SimpleDateFormat
import java.util.Locale

object RegisterRestaurantValidation {

    // Convert 12-hour time string ("10:00 PM") to total minutes from midnight for safe comparison
    private fun timeToMinutes(timeStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf.parse(timeStr.trim()) ?: return 0
            val cal = java.util.Calendar.getInstance().apply { time = date }
            cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        } catch (e: Exception) {
            0
        }
    }

    // Ensures cleanup time is strictly after closing time
    fun isCleanupValid(closingTime: String, cleanupTime: String): Boolean {
        return timeToMinutes(cleanupTime) > timeToMinutes(closingTime)
    }

    // Ensures SSM is strictly 12 digits
    fun isSsmValid(ssm: String): Boolean {
        return ssm.length == 12 && ssm.all { it.isDigit() }
    }
}