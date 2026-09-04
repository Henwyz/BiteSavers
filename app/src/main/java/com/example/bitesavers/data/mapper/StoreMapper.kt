package com.example.bitesavers.data.mapper

import com.example.bitesavers.business.profile.data.BusinessProfileUiModel
import com.example.bitesavers.data.remote.dto.StoreDto

fun StoreDto.toUiModel(): BusinessProfileUiModel {
    val open = openingTime ?: "08:30"
    val close = closingTime ?: "21:00"
    val cleanup = cleanupEndTime ?: "22:00"

    return BusinessProfileUiModel(
        storeId = id,
        businessName = name?.ifBlank { "My Business Store" } ?: "My Business Store",
        verificationId = ssmNumber?.ifBlank { id?.take(10)?.uppercase() ?: "DCM-2506744-B" }
            ?: id?.take(10)?.uppercase() ?: "DCM-2506744-B",
        isVerified = status?.equals("APPROVED", ignoreCase = true) ?: true,
        rating = rating ?: 4.8,
        reviewCount = 115,
        address = address?.ifBlank { "No address set" } ?: "No address set",
        phone = contactPhone?.ifBlank { "+60 12-345 6789" } ?: "+60 12-345 6789",
        category = "Food & Beverage",
        operatingHours = "$open - $close",
        cleanupHours = cleanup,
        latitude = latitude ?: 5.4674,
        longitude = longitude ?: 100.2790,
        walletBalance = 0.0 // Populated directly via users.wallet_balance in ViewModel
    )
}