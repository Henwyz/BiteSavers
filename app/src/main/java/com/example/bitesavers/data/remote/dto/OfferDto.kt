package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfferDto(
    @SerialName("id")
    val id: String,

    @SerialName("store_id")
    val storeId: String,

    @SerialName("storage_box_id")
    val storageBoxId: String? = null,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("category")
    val category: String,

    @SerialName("original_price")
    val originalPrice: Double,

    @SerialName("discounted_price")
    val discountedPrice: Double,

    @SerialName("weight_kg")
    val weightKg: Double,

    @SerialName("quantity_available")
    val quantityAvailable: Int,

    @SerialName("is_eligible_for_ngo_free")
    val isEligibleForNgoFree: Boolean = false,

    @SerialName("status")
    val status: String,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("pickup_start")
    val pickupStart: String? = null, // e.g. "20:30:00"

    @SerialName("pickup_end")
    val pickupEnd: String? = null // e.g. "21:30:00"
)