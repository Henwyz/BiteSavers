package com.example.bitesavers.business.analytics.logic

import androidx.lifecycle.ViewModel
import com.example.bitesavers.business.analytics.data.AnalyticsUiModel
import com.example.bitesavers.business.analytics.data.DailyMetricPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MVP-phase: dummy in-memory data matching the Figma mockup. Swap for real
 * aggregated order/listing data once completed orders are tracked
 * somewhere shared (likely wherever Member 1/2's checkout data lives).
 */
class AnalyticsViewModel : ViewModel() {

    private val _analytics = MutableStateFlow(
        AnalyticsUiModel(
            weekLabel = "Week of 7-13 Jul 2026",
            revenueRecoveredRM = 750.0,
            revenueChangePercent = 18,
            mealsRescued = 150,
            mealsChangePercent = 23,
            uniqueConsumers = 67,
            newConsumersThisWeek = 25,
            foodWasteSavedKg = 22.4,
            co2SavedKg = 13.4,
            weeklyRevenue = listOf(
                DailyMetricPoint("Mon", 70.0),
                DailyMetricPoint("Tue", 100.0),
                DailyMetricPoint("Wed", 60.0),
                DailyMetricPoint("Thu", 150.0),
                DailyMetricPoint("Fri", 170.0),
                DailyMetricPoint("Sat", 230.0),
                DailyMetricPoint("Sun", 180.0)
            ),
            mealsPerDay = listOf(
                DailyMetricPoint("Mon", 14.0),
                DailyMetricPoint("Tue", 18.0),
                DailyMetricPoint("Wed", 12.0),
                DailyMetricPoint("Thu", 22.0),
                DailyMetricPoint("Fri", 26.0),
                DailyMetricPoint("Sat", 34.0),
                DailyMetricPoint("Sun", 24.0)
            )
        )
    )
    val analytics: StateFlow<AnalyticsUiModel> = _analytics.asStateFlow()
}
