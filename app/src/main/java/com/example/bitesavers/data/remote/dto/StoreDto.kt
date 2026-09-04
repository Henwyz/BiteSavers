package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreDto(
    @SerialName("id") val id: String,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("name") val name: String,
    @SerialName("address") val address: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("rating") val rating: Double? = null,
    @SerialName("opening_time") val openingTime: String? = null,
    @SerialName("closing_time") val closingTime: String? = null,
    @SerialName("cleanup_end_time") val cleanupEndTime: String? = null,
    @SerialName("contact_phone") val contactPhone: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("ssm_number") val ssmNumber: String? = null,
    @SerialName("ssm_document_url") val ssmDocumentUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class StoreEditInsertDto(
    @SerialName("id") val id: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("name") val name: String,
    @SerialName("address") val address: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("rating") val rating: Double? = null,
    @SerialName("contact_phone") val contactPhone: String? = null,
    @SerialName("opening_time") val openingTime: String? = null,
    @SerialName("closing_time") val closingTime: String? = null,
    @SerialName("cleanup_end_time") val cleanupEndTime: String? = null,
    @SerialName("status") val status: String? = "PENDING",
    @SerialName("reason_for_change") val reasonForChange: String? = null
)