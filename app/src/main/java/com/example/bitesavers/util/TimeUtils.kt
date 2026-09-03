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

    /**
     * Formats an ISO-8601 or standard SQL timestamp into readable format (e.g., "04 Sep, 04:15 AM").
     * Falls back to raw string or current formatted time if parsing fails.
     */
    fun formatNotificationTimestamp(rawTimestamp: String?): String {
        if (rawTimestamp.isNullOrBlank()) return ""
        return try {
            // Clean up standard Supabase ISO format (e.g. 2026-09-04T04:15:30.123+00:00)
            val clean = rawTimestamp.replace("T", " ").substringBefore(".")
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val date = inputFormat.parse(clean) ?: return rawTimestamp

            // Converts to local device time zone for display
            val outputFormat = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getDefault()
            }
            outputFormat.format(date)
        } catch (e: Exception) {
            rawTimestamp
        }
    }
}