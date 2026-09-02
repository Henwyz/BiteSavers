package com.example.bitesavers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.repository.NotificationRepository
import com.example.bitesavers.data.repository.OfferRepository
import com.example.bitesavers.data.repository.OrderRepository
import com.example.bitesavers.navigation.AppNavHost
import com.example.bitesavers.ui.theme.BiteSaversTheme
import com.example.bitesavers.util.OrderNotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val orderRepository: OrderRepository = OrderRepository()
    private val offerRepository: OfferRepository = OfferRepository()
    private val notificationRepository: NotificationRepository = NotificationRepository()

    companion object {
        // Shared state flow observed by AppNavHost to handle notification click navigation
        val pendingOrderIdRoute = MutableStateFlow<String?>(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize persistent storage and restore any previously saved session
        UserSession.init(applicationContext)

        // Handles notification click if the app was launched from the notification bar
        handleNotificationIntent(intent)

        // Global in-app status observer: monitors active orders across any screen while the app is open
        startGlobalOrderObserver()

        setContent {
            BiteSaversTheme {
                AppNavHost()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handles notification click if the app was already running in the background/foreground
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val orderId = intent?.getStringExtra("EXTRA_ORDER_ID")
        if (!orderId.isNullOrBlank()) {
            pendingOrderIdRoute.value = orderId
        }
    }

    // Continuously checks user orders while MainActivity is in STARTED state
    private fun startGlobalOrderObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    val userId = UserSession.getUserId()
                    if (userId.isNotBlank()) {
                        try {
                            val orders = orderRepository.fetchOrdersByUserId(userId)
                            val notifiedBannerIds = UserSession.getNotifiedBannerOrderIds()

                            // Detect completed orders that have not yet had a system notification banner shown
                            val newlyCompletedOrders = orders.filter { order ->
                                !order.id.isNullOrBlank() &&
                                        order.status.equals("COMPLETED", ignoreCase = true) &&
                                        !notifiedBannerIds.contains(order.id)
                            }

                            for (order in newlyCompletedOrders) {
                                val orderId = order.id.orEmpty()
                                val offer = offerRepository.fetchOfferById(order.offerId)
                                val storeName = offer?.storeName ?: "BiteSavers Store"
                                val shortId = "BS-" + orderId.takeLast(5).uppercase()

                                // Shows the system status bar banner alert
                                OrderNotificationHelper.showOrderCompletedNotification(
                                    context = applicationContext,
                                    orderId = orderId,
                                    storeName = storeName
                                )

                                // Persists the completed notification into the remote user_notifications table
                                notificationRepository.insertNotification(
                                    id = "NOTIF_${System.currentTimeMillis()}_${orderId.takeLast(4)}",
                                    userId = userId,
                                    orderId = orderId,
                                    title = "Order Completed! 🎉",
                                    message = "Order $shortId has been picked up from $storeName. Tap to view ticket."
                                )

                                // Mark banner as shown so it does not repeat
                                UserSession.markBannerAsShown(orderId)

                                // Immediately signal DiscoveryViewModel to refresh the bell badge count
                                UserSession.notifyNewOrderUpdate()
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Global order observer error: ${e.message}")
                        }
                    }

                    // Polls every 4 seconds while the app is actively in the foreground
                    delay(4000)
                }
            }
        }
    }
}