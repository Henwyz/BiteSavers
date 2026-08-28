package com.example.bitesavers.business.dashboard.data

data class RecentOrderItem(
    val id: String,
    val title: String,
    val customerInfo: String,
    val price: Double
)

data class DashboardMetrics(
    val activeListingsCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val foodSoldToday: Int = 0,
    val soldGrowthText: String = "",
    val revenueRecovered: Double = 0.0,
    val wasteReducedKg: Double = 0.0,
    val co2SavedKg: Double = 0.0
)