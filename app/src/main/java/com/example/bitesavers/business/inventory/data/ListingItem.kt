package com.example.bitesavers.business.inventory.data

import android.graphics.Bitmap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ListingItem(
    val id: String = "",
    @SerialName("store_id")
    val storeId: String = "",

    @SerialName("storage_box_id")
    val storageBoxId: String? = null,

    @SerialName("title")
    val name: String = "",

    val description: String = "",
    val category: String = "",

    @SerialName("original_price")
    val originalPrice: Double = 0.0,

    @SerialName("discounted_price")
    val discountPrice: Double = 0.0,

    @SerialName("weight_kg")
    val weightKg: Double? = null,

    @SerialName("quantity_available")
    val quantity: Int = 0,

    @SerialName("pickup_start")
    val pickupStart: String? = null,

    @SerialName("pickup_end")
    val pickupEnd: String? = null,

    @SerialName("is_eligible_for_ngo_free")
    val isEligibleForNgoFree: Boolean = false,

    val status: String = "ACTIVE",

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @Transient
    val imageBitmap: Bitmap? = null
) {
    val discountPercent: Int
        get() = if (originalPrice > 0) {
            (((originalPrice - discountPrice) / originalPrice) * 100).toInt()
        } else 0
}