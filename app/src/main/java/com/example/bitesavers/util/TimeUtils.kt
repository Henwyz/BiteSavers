package com.example.bitesavers.util

import java.util.Calendar

object TimeUtils {

    /**
     * Converts a "HH:mm" or "HH:mm:ss" string into total minutes from midnight (0..1439).
     * Returns -1 if the string is empty or malformed.
     */
    fun timeStringToMinutes(rawTime: String?): Int {
        if (rawTime.isNullOrBlank()) return -1
        return try {
            val parts = rawTime.trim().split(":")
            val hours = parts.getOrNull(0)?.toIntOrNull() ?: return -1
            val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
            (hours * 60) + minutes
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * Returns the device's current time as minutes from midnight (0..1439).
     */
    fun getCurrentMinutesOfDay(): Int {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return (hour * 60) + minute
    }

    /**
     * Checks if current time is within [startRaw] and [endRaw].
     * Handles midnight crossover (e.g., 23:00 to 01:00) safely.
     */
    fun isCurrentTimeWithin(startRaw: String?, endRaw: String?): Boolean {
        val startMinutes = timeStringToMinutes(startRaw)
        val endMinutes = timeStringToMinutes(endRaw)
        if (startMinutes == -1 || endMinutes == -1) return false

        val now = getCurrentMinutesOfDay()
        return if (endMinutes >= startMinutes) {
            now in startMinutes..endMinutes
        } else {
            // Crossover past midnight
            now >= startMinutes || now <= endMinutes
        }
    }
}