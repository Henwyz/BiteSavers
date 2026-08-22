package com.example.bitesavers.business.analytics.data

data class DailyMetricPoint(val label: String, val value: Double)

data class AnalyticsUiModel(
    val weekLabel: String,
    val revenueRecoveredRM: Double,
    val revenueChangePercent: Int,
    val mealsRescued: Int,
    val mealsChangePercent: Int,
    val uniqueConsumers: Int,
    val newConsumersThisWeek: Int,
    val foodWasteSavedKg: Double,
    val co2SavedKg: Double,
    val weeklyRevenue: List<DailyMetricPoint>,
    val mealsPerDay: List<DailyMetricPoint>
)
