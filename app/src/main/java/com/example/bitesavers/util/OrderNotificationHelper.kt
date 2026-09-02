package com.example.bitesavers.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.bitesavers.MainActivity
import com.example.bitesavers.R

object OrderNotificationHelper {
    // Unique internal ID for our notification channel
    private const val CHANNEL_ID = "bitesaver_orders_channel"

    // Base number used to generate unique notification IDs so multiple orders don't overwrite each other
    private const val NOTIFICATION_ID_OFFSET = 7000

    /**
     * Registers a Notification Channel with Android.
     * Required for Android 8.0 (API level 26) and higher before posting any notification.
     */
    private fun createNotificationChannel(context: Context) {
        // Check if the user's phone is running Android 8.0 (Oreo) or newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Read localized channel name and description from strings.xml
            val name = context.getString(R.string.notification_channel_orders_name)
            val descriptionText = context.getString(R.string.notification_channel_orders_desc)

            // IMPORTANCE_HIGH makes the notification pop down from the top of the screen with sound
            val importance = NotificationManager.IMPORTANCE_HIGH

            // Create the channel object with ID, visible name, and priority level
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true) // Vibrates the phone when triggered
            }

            // Get Android's system notification service
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Register the channel with the operating system
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Builds and displays the heads-up notification when an order is completed.
     */
    fun showOrderCompletedNotification(
        context: Context,
        orderId: String,
        storeName: String
    ) {
        // Ensure the channel exists before trying to post a notification
        createNotificationChannel(context)

        // Access the system notification service that controls device alerts
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent defining what happens when the notification is tapped:
        // Launch MainActivity and bring it to the foreground
        val intent = Intent(context, MainActivity::class.java).apply {
            // Re-uses existing activity if already open instead of launching duplicate instances
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Attach the clean orderId so MainActivity knows which order ticket to open
            putExtra("EXTRA_ORDER_ID", orderId)
        }

        // Wrap the Intent in a PendingIntent.
        // A PendingIntent gives Android OS permission to execute our intent on our behalf when clicked.
        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.hashCode(), // Unique request code per order
            intent,
            // FLAG_UPDATE_CURRENT: updates any existing intent with newer extras
            // FLAG_IMMUTABLE: required security flag on Android 12+ (API 31+)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Formats clean display tag (e.g., ord_1001 -> #BS-1001)
        val cleanSuffix = orderId.removePrefix("ord_").takeLast(6).uppercase()
        val shortId = "#BS-$cleanSuffix"

        // Fetch strings with dynamic parameters (storeName and shortId) from strings.xml
        val title = context.getString(R.string.notification_order_completed_title)
        val shortContent = context.getString(R.string.notification_order_completed_short, storeName, shortId)
        val longContent = context.getString(R.string.notification_order_completed_long, storeName, shortId)

        // Construct the notification visual presentation
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // Small icon that appears in the top status bar
            .setSmallIcon(R.drawable.ic_verified)
            // Main bold header text
            .setContentTitle(title)
            // One-line preview summary
            .setContentText(shortContent)
            // BigTextStyle allows expanding the notification banner to read more details
            .setStyle(NotificationCompat.BigTextStyle().bigText(longContent))
            // High priority ensures it pops down as a banner while the user is using the phone
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // Attach the click action we prepared earlier
            .setContentIntent(pendingIntent)
            // Dismisses the notification automatically once the user taps on it
            .setAutoCancel(true)
            .build()

        // Calculate a unique notification ID using the hash of orderId
        val notificationId = NOTIFICATION_ID_OFFSET + (orderId.hashCode() % 1000)

        // Tell Android to display the notification immediately
        notificationManager.notify(notificationId, notification)
    }
}