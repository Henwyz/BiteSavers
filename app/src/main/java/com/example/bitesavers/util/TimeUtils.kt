package com.example.bitesavers.util

import java.util.Calendar
import java.util.TimeZone

object TimeUtils {

    // Malaysia Standard Time Zone (UTC+8)
    private val malaysiaTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")

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
     * Returns the current Malaysia time (GMT+8) as total minutes from midnight (0..1439).
     */
    fun getCurrentMinutesOfDay(): Int {
        val calendar = Calendar.getInstance(malaysiaTimeZone)
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
     * Converts UTC timestamps from Supabase into Malaysia time (GMT+8).
     */
    fun formatNotificationTimestamp(rawTimestamp: String?): String {
        if (rawTimestamp.isNullOrBlank()) return ""
        return try {
            // Clean up standard Supabase ISO format (e.g. 2026-09-04T04:15:30.123+00:00)
            val clean = rawTimestamp.replace("T", " ").substringBefore(".")
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = inputFormat.parse(clean) ?: return rawTimestamp

            // Converts to Malaysia time zone for consistent regional display
            val outputFormat = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).apply {
                timeZone = malaysiaTimeZone
            }
            outputFormat.format(date)
        } catch (e: Exception) {
            rawTimestamp
        }
    }
}