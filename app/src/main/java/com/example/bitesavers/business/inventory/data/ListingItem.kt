package com.example.bitesavers.business.inventory.data

import android.graphics.Bitmap
import com.example.bitesavers.data.remote.dto.OfferDto

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
    val isEligibleForNgoFree: Boolean = false,
    val status: String = "ACTIVE",
    val imageUrl: String? = null,
    val imageBitmap: Bitmap? = null
) {
    val discountPercent: Int
        get() = if (originalPrice > 0) {
            (((originalPrice - discountPrice) / originalPrice) * 100).toInt().coerceAtLeast(0)
        } else 0
}

fun OfferDto.toListingItem(defaultStart: String, defaultEnd: String): ListingItem {
    return ListingItem(
        id = this.id,
        storeId = this.storeId ?: "",
        storageBoxId = this.storageBoxId,
        name = this.title,
        description = this.description ?: "",
        category = this.category ?: "Bakery",
        originalPrice = this.originalPrice,
        discountPrice = this.discountedPrice,
        weightKg = this.weightKg,
        quantity = this.quantityAvailable,
        pickupStart = this.pickupStart ?: defaultStart,
        pickupEnd = this.pickupEnd ?: defaultEnd,
        status = this.status,
        imageUrl = this.imageUrl,
        isEligibleForNgoFree = this.isEligibleForNgoFree || this.discountedPrice == 0.0
    )
}

fun ListingItem.toDto(resolvedStoreId: String): OfferDto {
    // Flag as true if explicitly marked, category is Free, or discountPrice is 0.0
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