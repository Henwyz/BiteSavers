package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodDto(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String,
    @SerialName("type") val type: String,
    @SerialName("card_holder") val cardHolder: String? = null,
    @SerialName("last_four_digits") val lastFourDigits: String? = null,
    @SerialName("expiry_date") val expiryDate: String? = null,
    @SerialName("linked_phone") val linkedPhone: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)