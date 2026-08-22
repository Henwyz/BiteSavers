package com.example.bitesavers.business.profile.data

data class OperatingHoursRow(val dayRangeLabel: String, val hoursLabel: String)

data class BusinessProfileUiModel(
    val businessName: String,
    val verificationId: String,
    val isVerified: Boolean,
    val rating: Double,
    val reviewCount: Int,
    val address: String,
    val phone: String,
    val category: String,
    val operatingHours: List<OperatingHoursRow>
)
