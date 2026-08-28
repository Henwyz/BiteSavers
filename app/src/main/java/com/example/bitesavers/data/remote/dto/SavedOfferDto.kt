package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavedOfferDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("offer_id") val offerId: String,
    @SerialName("created_at") val createdAt: String? = null
)