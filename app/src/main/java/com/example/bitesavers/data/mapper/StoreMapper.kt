package com.example.bitesavers.data.mapper

import com.example.bitesavers.business.profile.data.BusinessProfileUiModel
import com.example.bitesavers.data.remote.dto.StoreDto

fun StoreDto.toUiModel(): BusinessProfileUiModel {
    val open = openingTime ?: "08:30"
    val close = closingTime ?: "21:00"
    val cleanup = cleanupEndTime ?: "22:00"

    return BusinessProfileUiModel(
        businessName = name?.ifBlank { "My Business Store" } ?: "My Business Store",
        verificationId = id?.take(10)?.uppercase() ?: "DCM-2506744-B",
        isVerified = true,
        rating = rating ?: 4.8,
        reviewCount = 115,
        address = address?.ifBlank { "No address set" } ?: "No address set",
        phone = contactPhone?.ifBlank { "+60 12-345 6789" } ?: "+60 12-345 6789",
        category = "Food & Beverage",
        operatingHours = "$open - $close",
        cleanupHours = cleanup
    )
}