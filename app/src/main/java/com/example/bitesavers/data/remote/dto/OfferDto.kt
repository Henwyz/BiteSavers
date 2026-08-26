package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfferDto(
    val id: String,
    @SerialName("store_id") val storeId: String? = null,
    @SerialName("storage_box_id") val storageBoxId: String? = null,
    val title: String,
    val description: String? = null,
    val category: String? = null,
    @SerialName("original_price") val originalPrice: Double = 0.0,
    @SerialName("discounted_price") val discountedPrice: Double = 0.0,
    @SerialName("weight_kg") val weightKg: Double = 0.5,
    @SerialName("quantity_available") val quantityAvailable: Int = 0,
    @SerialName("is_eligible_for_ngo_free") val isEligibleForNgoFree: Boolean = false,
    val status: String = "ACTIVE",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)