package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfferDto(
    val id: String,
    val title: String,
    @SerialName("store_name") val storeName: String,
    @SerialName("original_price") val originalPrice: Double,
    @SerialName("current_price") val currentPrice: Double,
    @SerialName("quantity_left") val quantityLeft: Int,
    @SerialName("hours_to_close") val hoursToClose: Int,
    @SerialName("pickup_window") val pickupWindow: String? = null,
    val category: String,
    @SerialName("is_eligible_for_ngo_free") val isEligibleForNgoFree: Boolean = false,
    @SerialName("image_url") val imageUrl: String? = null,
    val description: String? = null,
    @SerialName("live_temperature") val liveTemperature: Double? = null,
    @SerialName("storage_type") val storageType: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)