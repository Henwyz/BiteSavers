package com.example.bitesavers.business.dashboard.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.business.dashboard.data.CheckOrderData
import com.example.bitesavers.business.dashboard.data.DashboardMetrics
import com.example.bitesavers.business.inventory.data.ListingItem
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.NotificationDto
import com.example.bitesavers.data.remote.dto.StoreDto
import com.example.bitesavers.data.repository.NotificationRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel : ViewModel() {
    private val _recentOrders = MutableStateFlow<List<CheckOrderData>>(emptyList())
    val recentOrders: StateFlow<List<CheckOrderData>> = _recentOrders.asStateFlow()

    private val notificationRepository = NotificationRepository()

    private val _notifications = MutableStateFlow<List<NotificationDto>>(emptyList())
    val notifications: StateFlow<List<NotificationDto>> = _notifications.asStateFlow()

    // Count unread notifications to display on the bell badge
    val unreadCount = combine(_notifications) { notifs ->
        notifs.first().count { !it.isRead }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Observable UI state for store details and current date
    var currentStoreId by mutableStateOf("")
        private set
    var storeName by mutableStateOf("Loading...")
        private set
    var storeInitials by mutableStateOf("--")
        private set
    var currentDateFormatted by mutableStateOf("")
        private set
    var hasCheckedStore by mutableStateOf(false)
        private set
    var requiresRegistration by mutableStateOf(false)
        private set

    init {
        // Format device date dynamically (e.g. Wednesday, 2 Sep 2026)
        val sdf = SimpleDateFormat("EEEE, d MMM yyyy", Locale.US)
        currentDateFormatted = sdf.format(Date())

        // Initialize store resolution and fetch associated orders
        loadCurrentStoreAndOrders()
    }

    fun loadCurrentStoreAndOrders() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = UserSession.getUserId()
            if (userId.isNotBlank()) {
                try {
                    // Query stores where owner_id matches current authenticated user
                    val stores = SupabaseClient.client.from("stores")
                        .select {
                            filter {
                                eq("owner_id", userId)
                            }
                        }
                        .decodeList<StoreDto>()

                    val myStore = stores.firstOrNull()
                    if (myStore != null && myStore.name.isNotBlank()) {
                        currentStoreId = myStore.id
                        storeName = myStore.name
                        storeInitials = computeInitials(storeName)
                        requiresRegistration = false
                    } else {
                        try {
                            val userRecords = SupabaseClient.client.from("users")
                                .select {
                                    filter { eq("id", userId) }
                                }
                                .decodeList<Map<String, kotlinx.serialization.json.JsonElement>>()

                            val userName = userRecords.firstOrNull()?.get("name")
                                ?.toString()?.trim('"')

                            if (!userName.isNullOrBlank()) {
                                storeName = userName
                                storeInitials = computeInitials(userName)
                            } else {
                                storeName = "My Store"
                                storeInitials = "MS"
                            }
                        } catch (e: Exception) {
                            storeName = "My Store"
                            storeInitials = "MS"
                        }
                        currentStoreId = myStore?.id ?: ""
                        requiresRegistration = (myStore == null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    storeName = "My Store"
                    storeInitials = "MS"
                    requiresRegistration = false
                }
            } else {
                storeName = "Guest Merchant"
                storeInitials = "GM"
                requiresRegistration = false
            }

            hasCheckedStore = true
            // Fetch live orders filtered strictly by the resolved store ID
            fetchOrdersFromSupabase()
            fetchMerchantNotifications()
        }
    }

    fun fetchOrdersFromSupabase() {
        viewModelScope.launch(Dispatchers.IO) {
            // Guard clause: abort query if no store is linked
            if (currentStoreId.isBlank()) {
                _recentOrders.value = emptyList()
                return@launch
            }

            try {
                // PostgREST join query filtered strictly by store_id
                val response = SupabaseClient.client.from("orders").select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, offers(*)")
                ) {
                    filter {
                        eq("store_id", currentStoreId)
                    }
                }
                val liveOrders = response.decodeList<CheckOrderData>()
                _recentOrders.value = liveOrders
            } catch (e: Exception) {
                e.printStackTrace()
                _recentOrders.value = emptyList()
            }
        }
    }

    // to generate dynamic 2-letter store initials
    private fun computeInitials(name: String): String {
        val words = name.trim().split("\\s+".toRegex())
        return when {
            words.size >= 2 -> "${words[0].firstOrNull() ?: ""}${words[1].firstOrNull() ?: ""}".uppercase()
            words.isNotEmpty() && words[0].isNotEmpty() -> words[0].take(2).uppercase()
            else -> "BS"
        }
    }

    // Dynamically calculate metrics using real inventory data stream
    fun getDynamicMetrics(listingsFlow: StateFlow<List<ListingItem>>): StateFlow<DashboardMetrics> {
        return combine(listingsFlow, _recentOrders) { listings, orders ->
            val activeListings = listings.count { it.status.equals("Active", ignoreCase = true) }
            val expiringSoon = listings.count { it.status.equals("Active", ignoreCase = true) && it.quantity <= 2 }
            val soldCount = orders.count { it.status.equals("COMPLETED", ignoreCase = true) }
            val revenue = orders.filter { it.status.equals("COMPLETED", ignoreCase = true) }.sumOf { it.totalPrice }
            val wasteKg = orders.filter { it.status.equals("COMPLETED", ignoreCase = true) }.sumOf { it.totalWeightKg }
            val co2Kg = wasteKg * 0.55

            DashboardMetrics(
                activeListingsCount = activeListings,
                expiringSoonCount = expiringSoon,
                foodSoldToday = soldCount,
                soldGrowthText = "Live Data",
                revenueRecovered = revenue,
                wasteReducedKg = (wasteKg * 10).toInt() / 10.0,
                co2SavedKg = (co2Kg * 10).toInt() / 10.0
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardMetrics()
        )
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SupabaseClient.client.from("orders").delete {
                    filter { eq("id", orderId) }
                }
                // refresh list again
                fetchOrdersFromSupabase()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Fetch notifications for the current authenticated merchant
    fun fetchMerchantNotifications() {
        val userId = com.example.bitesavers.data.remote.UserSession.getUserId()
        if (userId.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val list = notificationRepository.fetchUserNotifications(userId)
            _notifications.value = list
        }
    }

    // Mark notifications as read
    fun markNotificationsAsRead() {
        val unreadIds = _notifications.value.filter { !it.isRead }.map { it.id }
        if (unreadIds.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val success = notificationRepository.markAsRead(unreadIds)
            if (success) {
                fetchMerchantNotifications()
            }
        }
    }

    // Clear all notifications for the merchant
    fun clearAllNotifications() {
        val userId = com.example.bitesavers.data.remote.UserSession.getUserId()
        if (userId.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val success = notificationRepository.clearAllNotifications(userId)
            if (success) {
                _notifications.value = emptyList()
            }
        }
    }
}

