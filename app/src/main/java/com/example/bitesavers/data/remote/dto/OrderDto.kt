package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String? = null,
    @SerialName("offer_id") val offerId: String,
    @SerialName("user_role") val userRole: String,
    val quantity: Int,
    @SerialName("total_price") val totalPrice: Double,
    val status: String,
    @SerialName("pickup_window_close") val pickupWindowClose: String
)