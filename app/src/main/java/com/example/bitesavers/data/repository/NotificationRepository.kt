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

    private fun generateNotificationId(): String = "notif_${System.currentTimeMillis().toString().takeLast(6)}"

    suspend fun fetchUserNotifications(userId: String): List<NotificationDto> = withContext(Dispatchers.IO) {
        try {
            client.from("user_notifications")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(20)
                }
                .decodeList<NotificationDto>()
        } catch (e: Exception) {
            Log.e("NotificationRepo", "fetchUserNotifications error: ${e.message}", e)
            emptyList()
        }
    }

    // Checks if a notification for this specific order already exists in Supabase to prevent duplicate spam
    suspend fun hasNotificationForOrder(userId: String, orderId: String): Boolean = withContext(Dispatchers.IO) {
        if (orderId.isBlank()) return@withContext false
        try {
            val existing = client.from("user_notifications")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("order_id", orderId)
                    }
                    limit(1)
                }
                .decodeList<NotificationDto>()
            existing.isNotEmpty()
        } catch (e: Exception) {
            Log.e("NotificationRepo", "hasNotificationForOrder check failed: ${e.message}")
            false
        }
    }

    // Inserts a new user notification only if it has not already been created for this order
    suspend fun insertNotification(
        userId: String,
        title: String,
        message: String,
        orderId: String? = null,
        id: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check for existing notification for this order
            if (!orderId.isNullOrBlank()) {
                val exists = hasNotificationForOrder(userId, orderId)
                if (exists) {
                    Log.d("NotificationRepo", "Notification already exists for order $orderId, skipping insert.")
                    return@withContext true
                }
            }

            // Use deterministic ID based on orderId when possible to prevent primary key duplicates
            val resolvedId = when {
                !id.isNullOrBlank() -> id
                !orderId.isNullOrBlank() -> "notif_${orderId.removePrefix("ord_")}"
                else -> generateNotificationId()
            }

            val dto = NotificationDto(
                id = resolvedId,
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