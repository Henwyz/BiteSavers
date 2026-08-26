package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String = "u1",
    val type: String, // 'BANK_CARD', 'TNG_EWALLET'
    @SerialName("card_holder") val cardHolder: String? = null,
    @SerialName("last_four_digits") val lastFourDigits: String? = null,
    @SerialName("expiry_date") val expiryDate: String? = null,
    @SerialName("linked_phone") val linkedPhone: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false
)