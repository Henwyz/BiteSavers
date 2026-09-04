package com.example.bitesavers.business.analytics.logic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.business.analytics.data.AnalyticsUiModel
import com.example.bitesavers.business.analytics.data.DailyMetricPoint
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.OrderDto
import com.example.bitesavers.data.remote.dto.StoreDto
import com.example.bitesavers.data.remote.repository.ProfileRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AnalyticsViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _analytics = MutableStateFlow(
        AnalyticsUiModel(
            weekLabel = "Loading...",
            revenueRecoveredRM = 0.0,
            revenueChangePercent = 0,
            mealsRescued = 0,
            mealsChangePercent = 0,
            uniqueConsumers = 0,
            newConsumersThisWeek = 0,
            foodWasteSavedKg = 0.0,
            co2SavedKg = 0.0,
            weeklyRevenue = emptyDayPoints(),
            mealsPerDay = emptyDayPoints()
        )
    )
    val analytics: StateFlow<AnalyticsUiModel> = _analytics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Get current logged-in business owner userId
                val userId = UserSession.getUserId().ifBlank { UserSession.currentUserId.value }
                Log.d("AnalyticsViewModel", "Loading analytics for userId: '$userId'")

                // 2. Fetch the active APPROVED store for this owner
                var targetStoreId: String? = null
                if (userId.isNotBlank()) {
                    try {
                        val stores = SupabaseClient.client.from("stores")
                            .select { filter { eq("owner_id", userId) } }
                            .decodeList<StoreDto>()

                        // Strictly select the approved active store row to match Profile & Inventory
                        val activeStore = stores.firstOrNull { it.status?.equals("APPROVED", ignoreCase = true) == true }
                            ?: stores.lastOrNull { it.status?.equals("PENDING", ignoreCase = true) != true }
                            ?: stores.firstOrNull()

                        targetStoreId = activeStore?.id
                    } catch (e: Exception) {
                        Log.w("AnalyticsViewModel", "Error fetching store for owner: ${e.message}")
                    }
                }

                Log.d("AnalyticsViewModel", "Target storeId strictly for analytics: $targetStoreId")

                // 3. Fetch orders strictly filtered by this store's store_id
                val allOrders = mutableListOf<OrderDto>()
                if (!targetStoreId.isNullOrBlank()) {
                    val orders = repository.getOrdersByStoreId(targetStoreId)
                    allOrders.addAll(orders)
                }

                Log.d("AnalyticsViewModel", "Total store orders retrieved: ${allOrders.size}")

                // 4. Compute analytics strictly for this store's dataset
                val computed = calculateAnalytics(allOrders)
                _analytics.value = computed
                Log.d("AnalyticsViewModel", "Analytics computed: Revenue=RM${computed.revenueRecoveredRM}, Meals=${computed.mealsRescued}")
            } catch (e: Exception) {
                Log.e("AnalyticsViewModel", "Error computing analytics", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateAnalytics(allOrders: List<OrderDto>): AnalyticsUiModel {
        val (startOfThisWeek, endOfThisWeek, startOfLastWeek, endOfLastWeek, weekLabel) = getWeekDateRanges()

        // Include COMPLETED orders (case-insensitive)
        val completedOrders = allOrders.filter {
            it.status.isNullOrBlank() || it.status.equals("COMPLETED", ignoreCase = true)
        }

        val thisWeekOrders = completedOrders.filter { order ->
            val time = parseTimestamp(order.createdAt) ?: 0L
            time in startOfThisWeek..endOfThisWeek
        }

        val lastWeekOrders = completedOrders.filter { order ->
            val time = parseTimestamp(order.createdAt) ?: 0L
            time in startOfLastWeek until endOfLastWeek
        }

        // Use this week's orders if present, otherwise aggregate all available completed orders for this store
        val activeWorkingOrders = if (thisWeekOrders.isNotEmpty()) thisWeekOrders else completedOrders

        // 1. Metric: Revenue Recovered (RM)
        val thisWeekRevenue = activeWorkingOrders.filterNot { it.isNgoFreeClaim }.sumOf { it.totalPrice }
        val lastWeekRevenue = lastWeekOrders.filterNot { it.isNgoFreeClaim }.sumOf { it.totalPrice }
        val revenuePercentChange = calculatePercentChange(thisWeekRevenue, lastWeekRevenue)

        // 2. Metric: Meals Rescued (Quantity)
        val thisWeekMeals = activeWorkingOrders.sumOf { it.quantity }
        val lastWeekMeals = lastWeekOrders.sumOf { it.quantity }
        val mealsPercentChange = calculatePercentChange(thisWeekMeals.toDouble(), lastWeekMeals.toDouble())

        // 3. Metric: Unique Consumers
        val thisWeekUsers = activeWorkingOrders.mapNotNull { it.userId }.distinct()
        val uniqueConsumersCount = if (thisWeekUsers.isNotEmpty()) thisWeekUsers.size else activeWorkingOrders.size

        val priorUsers = completedOrders.filter {
            val time = parseTimestamp(it.createdAt) ?: 0L
            time < startOfThisWeek
        }.mapNotNull { it.userId }.toSet()

        val newConsumersCount = thisWeekUsers.count { it !in priorUsers }

        // 4. Metric: Food Waste Saved & CO2
        val totalWasteKg = activeWorkingOrders.sumOf {
            it.totalWeightKg ?: (it.quantity * 0.5)
        }
        val co2SavedKg = totalWasteKg * 2.5

        // 5. Daily Aggregation for Charts (Mon -> Sun)
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dailyRevenueMap = mutableMapOf("Mon" to 0.0, "Tue" to 0.0, "Wed" to 0.0, "Thu" to 0.0, "Fri" to 0.0, "Sat" to 0.0, "Sun" to 0.0)
        val dailyMealsMap = mutableMapOf("Mon" to 0.0, "Tue" to 0.0, "Wed" to 0.0, "Thu" to 0.0, "Fri" to 0.0, "Sat" to 0.0, "Sun" to 0.0)

        activeWorkingOrders.forEach { order ->
            val timestamp = parseTimestamp(order.createdAt)
            val dayKey = if (timestamp != null) getDayOfWeekLabel(timestamp) else "Thu"
            if (dayKey in dailyRevenueMap) {
                if (!order.isNgoFreeClaim) {
                    dailyRevenueMap[dayKey] = dailyRevenueMap[dayKey]!! + order.totalPrice
                }
                dailyMealsMap[dayKey] = dailyMealsMap[dayKey]!! + order.quantity
            }
        }

        val weeklyRevenuePoints = dayLabels.map { DailyMetricPoint(it, dailyRevenueMap[it] ?: 0.0) }
        val mealsPerDayPoints = dayLabels.map { DailyMetricPoint(it, dailyMealsMap[it] ?: 0.0) }

        return AnalyticsUiModel(
            weekLabel = weekLabel,
            revenueRecoveredRM = thisWeekRevenue,
            revenueChangePercent = revenuePercentChange,
            mealsRescued = thisWeekMeals,
            mealsChangePercent = mealsPercentChange,
            uniqueConsumers = uniqueConsumersCount,
            newConsumersThisWeek = if (newConsumersCount > 0) newConsumersCount else uniqueConsumersCount,
            foodWasteSavedKg = Math.round(totalWasteKg * 10.0) / 10.0,
            co2SavedKg = Math.round(co2SavedKg * 10.0) / 10.0,
            weeklyRevenue = weeklyRevenuePoints,
            mealsPerDay = mealsPerDayPoints
        )
    }

    private fun calculatePercentChange(current: Double, previous: Double): Int {
        if (previous <= 0.0) return if (current > 0.0) 100 else 0
        return (((current - previous) / previous) * 100).toInt()
    }

    // Resilient timestamp parser handling any Postgres / Supabase format
    private fun parseTimestamp(isoString: String?): Long? {
        if (isoString.isNullOrBlank()) return null
        val cleaned = isoString.trim().replace(" ", "T")

        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(cleaned)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        try {
            val datePart = cleaned.take(10)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.parse(datePart)?.time
        } catch (_: Exception) {}
        return null
    }

    private fun getDayOfWeekLabel(timestamp: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            Calendar.SUNDAY -> "Sun"
            else -> "Mon"
        }
    }

    private data class WeekDateRanges(
        val startOfThisWeek: Long,
        val endOfThisWeek: Long,
        val startOfLastWeek: Long,
        val endOfLastWeek: Long,
        val label: String
    )

    private fun getWeekDateRanges(): WeekDateRanges {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val startThisWeek = cal.timeInMillis
        val startDay = cal.get(Calendar.DAY_OF_MONTH)
        val startMonth = SimpleDateFormat("MMM", Locale.US).format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endThisWeek = cal.timeInMillis

        val endDay = cal.get(Calendar.DAY_OF_MONTH)
        val endMonth = SimpleDateFormat("MMM yyyy", Locale.US).format(cal.time)

        val weekLabel = if (startMonth == SimpleDateFormat("MMM", Locale.US).format(cal.time)) {
            "Week of $startDay-$endDay $endMonth"
        } else {
            "Week of $startDay $startMonth - $endDay $endMonth"
        }

        cal.timeInMillis = startThisWeek
        cal.add(Calendar.DAY_OF_YEAR, -7)
        val startLastWeek = cal.timeInMillis
        val endLastWeek = startThisWeek - 1

        return WeekDateRanges(startThisWeek, endThisWeek, startLastWeek, endLastWeek, weekLabel)
    }

    private fun emptyDayPoints() = listOf(
        DailyMetricPoint("Mon", 0.0),
        DailyMetricPoint("Tue", 0.0),
        DailyMetricPoint("Wed", 0.0),
        DailyMetricPoint("Thu", 0.0),
        DailyMetricPoint("Fri", 0.0),
        DailyMetricPoint("Sat", 0.0),
        DailyMetricPoint("Sun", 0.0)
    )
}