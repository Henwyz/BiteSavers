package com.example.bitesavers.business.inventory.data

import android.graphics.Bitmap
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.util.TimeUtils

data class ListingItem(
    val id: String = "",
    val storeId: String = "",
    val storageBoxId: String? = null,
    val name: String = "",
    val description: String = "",
    val category: String = "Bakery",
    val originalPrice: Double = 0.0,
    val discountPrice: Double = 0.0,
    val weightKg: Double = 0.35,
    val quantity: Int = 1,
    val pickupStart: String = "09:00 PM",
    val pickupEnd: String = "09:30 PM",
    val cleanupEndTime: String = "22:15:00",
    val isEligibleForNgoFree: Boolean = false,
    val status: String = "ACTIVE",
    val imageUrl: String? = null,
    val imageBitmap: Bitmap? = null
) {
    val discountPercent: Int
        get() = if (originalPrice > 0) {
            (((originalPrice - discountPrice) / originalPrice) * 100).toInt().coerceAtLeast(0)
        } else 0

    /**
     * Resolves the badge text and state dynamically:
     * - SOLD OUT: When remaining inventory is 0
     * - NGO CLAIM: When regular consumer pickup has passed, but within 1-hour NGO rescue window
     * - ACTIVE: When inside normal consumer pickup hours
     * - PAUSED: When outside pickup windows and not eligible for NGO claim
     */
    val displayBadgeStatus: String
        get() {
            if (quantity <= 0) return "SOLD OUT"

            val currentMinutes = TimeUtils.getCurrentMinutesOfDay()
            val pickupEndMinutes = TimeUtils.timeStringToMinutes(pickupEnd)

            if (pickupEndMinutes != -1) {
                val oneHourAfter = pickupEndMinutes + 60
                // Active 1-hour NGO claim window post-pickup
                if (currentMinutes in pickupEndMinutes..oneHourAfter) {
                    return "NGO CLAIM"
                }
                if (currentMinutes > oneHourAfter) {
                    return "EXPIRED"
                }
            }

            return if (status.equals("ACTIVE", ignoreCase = true)) "ACTIVE" else "PAUSED"
        }

    val isNgoWindowActive: Boolean
        get() {
            if (quantity <= 0) return false
            val currentMinutes = TimeUtils.getCurrentMinutesOfDay()
            val pickupEndMinutes = TimeUtils.timeStringToMinutes(pickupEnd)
            return pickupEndMinutes != -1 && currentMinutes in pickupEndMinutes..(pickupEndMinutes + 60)
        }
}

fun OfferDto.toListingItem(defaultStart: String, defaultEnd: String, defaultCleanup: String): ListingItem {
    return ListingItem(
        id = this.id,
        storeId = this.storeId ?: "",
        storageBoxId = this.storageBoxId,
        name = this.title,
        description = this.description ?: "",
        category = this.category ?: "Bakery",
        originalPrice = this.originalPrice,
        discountPrice = this.discountedPrice,
        weightKg = this.weightKg ?: 0.35,
        quantity = this.quantityAvailable ?: 0,
        pickupStart = this.pickupStart ?: defaultStart,
        pickupEnd = this.pickupEnd ?: defaultEnd,
        cleanupEndTime = defaultCleanup,
        status = this.status ?: "ACTIVE",
        imageUrl = this.imageUrl,
        isEligibleForNgoFree = this.isEligibleForNgoFree || this.discountedPrice == 0.0
    )
}

fun ListingItem.toDto(resolvedStoreId: String): OfferDto {
    val eligibleForNgo = this.isEligibleForNgoFree ||
            this.discountPrice == 0.0 ||
            this.category.equals("Free", ignoreCase = true)

    return OfferDto(
        id = this.id,
        storeId = resolvedStoreId,
        storageBoxId = this.storageBoxId,
        title = this.name,
        description = this.description.ifBlank { null },
        category = this.category,
        originalPrice = this.originalPrice,
        discountedPrice = this.discountPrice,
        weightKg = this.weightKg,
        quantityAvailable = this.quantity,
        pickupStart = this.pickupStart,
        pickupEnd = this.pickupEnd,
        status = this.status,
        imageUrl = this.imageUrl,
        isEligibleForNgoFree = eligibleForNgo
    )
}