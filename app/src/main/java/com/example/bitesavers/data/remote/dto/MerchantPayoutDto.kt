package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MerchantPayoutDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("store_id") val storeId: String,
    @SerialName("amount") val amount: Double,
    @SerialName("card_number") val cardNumber: String,
    @SerialName("status") val status: String? = "PENDING",
    @SerialName("created_at") val createdAt: String? = null
)