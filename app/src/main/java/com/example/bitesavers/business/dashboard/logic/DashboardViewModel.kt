package com.example.bitesavers.business.dashboard.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.business.dashboard.data.DashboardMetrics
import com.example.bitesavers.business.dashboard.data.RecentOrderItem
import com.example.bitesavers.business.inventory.data.ListingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


class DashboardViewModel : ViewModel() {

    //Simulated real-time orders list
    private val _recentOrders = MutableStateFlow(
        listOf(
            RecentOrderItem("1", "Butter Croissant x2", "Customer #A291 · 6 min ago", 3.00),
            RecentOrderItem("2", "Blueberry Muffin x1", "Customer #B154 · 23 min ago", 2.00),
            RecentOrderItem("3", "Sourdough Loaf x1", "Customer #C882 · 1 hr ago", 5.00)
        )
    )
    val recentOrders: StateFlow<List<RecentOrderItem>> = _recentOrders.asStateFlow()

    // Dynamically calculate metrics using real inventory data stream
    fun getDynamicMetrics(listingsFlow: StateFlow<List<ListingItem>>): StateFlow<DashboardMetrics> {
        return combine(listingsFlow, _recentOrders) { listings, orders ->
            val activeListings = listings.count { it.status.equals("Active", ignoreCase = true) }
            val expiringSoon =
                listings.count { it.status.equals("Active", ignoreCase = true) && it.quantity <= 2 }
            val soldCount = orders.size * 2
            val revenue = orders.sumOf { it.price }
            val wasteKg = soldCount * 0.4
            val co2Kg = wasteKg * 0.55

            DashboardMetrics(
                activeListingsCount = activeListings,
                expiringSoonCount = expiringSoon,
                foodSoldToday = soldCount,
                soldGrowthText = "↑ 12% vs yesterday",
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
}