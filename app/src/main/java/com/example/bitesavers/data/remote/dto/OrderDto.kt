package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("store_id") val storeId: String? = null,
    @SerialName("offer_id") val offerId: String? = null,
    @SerialName("quantity") val quantity: Int = 1,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("total_weight_kg") val totalWeightKg: Double? = null,
    @SerialName("is_ngo_free_claim") val isNgoFreeClaim: Boolean = false,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("status") val status: String? = "PENDING", // COMPLETED, PENDING, CANCELLED
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("remark") val remark: String? = null,
    @SerialName("pickup_pin") val pickupPin: String? = null
)