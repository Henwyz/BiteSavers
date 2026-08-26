package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("store_id") val storeId: String,
    @SerialName("offer_id") val offerId: String,
    val quantity: Int,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("total_weight_kg") val totalWeightKg: Double,
    @SerialName("is_ngo_free_claim") val isNgoFreeClaim: Boolean,
    @SerialName("payment_method") val paymentMethod: String,
    val status: String = "READY_FOR_PICKUP",
    @SerialName("created_at") val createdAt: String? = null
)