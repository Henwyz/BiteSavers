package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("offer_id")
    val offerId: String,

    @SerialName("user_role")
    val userRole: String? = null,

    @SerialName("quantity")
    val quantity: Int,

    @SerialName("total_price")
    val totalPrice: Double,

    @SerialName("status")
    val status: String = "READY_FOR_PICKUP",

    @SerialName("pickup_window_close")
    val pickupWindowClose: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)