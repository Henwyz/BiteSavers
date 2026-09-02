package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreDto(
    val id: String,
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rating: Double = 4.8,
    @SerialName("contact_phone") val contactPhone: String? = null,
    @SerialName("opening_time") val openingTime: String? = null,
    @SerialName("closing_time") val closingTime: String? = null,
    @SerialName("cleanup_end_time") val cleanupEndTime: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("ssm_number") val ssmNumber: String? = null,
    @SerialName("ssm_document_url") val ssmDocumentUrl: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false

)