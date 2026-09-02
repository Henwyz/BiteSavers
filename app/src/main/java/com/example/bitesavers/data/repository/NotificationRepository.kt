package com.example.bitesavers.data.repository

import android.util.Log
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.NotificationDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository {

    private val client = SupabaseClient.client

    // Generates human-readable ID matching Supabase text ID convention (e.g. notif_172530)
    private fun generateNotificationId(): String = "notif_${System.currentTimeMillis().toString().takeLast(6)}"

    suspend fun fetchUserNotifications(userId: String): List<NotificationDto> = withContext(Dispatchers.IO) {
        try {
            client.from("user_notifications")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(10)
                }
                .decodeList<NotificationDto>()
        } catch (e: Exception) {
            Log.e("NotificationRepo", "fetchUserNotifications error: ${e.message}", e)
            emptyList()
        }
    }

    // Inserts a new user notification with an auto-generated notif_ prefix ID if none is supplied
    suspend fun insertNotification(
        userId: String,
        title: String,
        message: String,
        orderId: String? = null,
        id: String = generateNotificationId()
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val dto = NotificationDto(
                id = id.ifBlank { generateNotificationId() },
                userId = userId,
                orderId = orderId,
                title = title,
                message = message,
                isRead = false
            )
            client.from("user_notifications").insert(dto)
            true
        } catch (e: Exception) {
            Log.e("NotificationRepo", "insertNotification error: ${e.message}", e)
            false
        }
    }

    suspend fun markAsRead(notificationIds: List<String>): Boolean = withContext(Dispatchers.IO) {
        if (notificationIds.isEmpty()) return@withContext true
        try {
            client.from("user_notifications")
                .update({
                    set("is_read", true)
                }) {
                    filter { isIn("id", notificationIds) }
                }
            true
        } catch (e: Exception) {
            Log.e("NotificationRepo", "markAsRead error: ${e.message}", e)
            false
        }
    }

    suspend fun clearAllNotifications(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("user_notifications")
                .delete {
                    filter { eq("user_id", userId) }
                }
            true
        } catch (e: Exception) {
            Log.e("NotificationRepo", "clearAllNotifications error: ${e.message}", e)
            false
        }
    }
}