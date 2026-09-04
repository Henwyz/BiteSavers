package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreDto(
    @SerialName("id")
    val id: String = "", // 👈 Non-null String (resolves Dashboard, Inventory & StoreDetail type mismatches)

    @SerialName("owner_id")
    val ownerId: String? = null,

    @SerialName("name")
    val name: String = "", // 👈 Non-null String (resolves isNotBlank & assignment errors)

    @SerialName("address")
    val address: String = "", // 👈 Non-null String (resolves StoreMapper & StoreRepository errors)

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null,

    @SerialName("rating")
    val rating: Double? = null,

    @SerialName("contact_phone")
    val contactPhone: String? = null,

    @SerialName("opening_time")
    val openingTime: String? = null,

    @SerialName("closing_time")
    val closingTime: String? = null,

    @SerialName("cleanup_end_time")
    val cleanupEndTime: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("reason_for_change")
    val reasonForChange: String? = null,

    @SerialName("ssm_number")
    val ssmNumber: String? = null,

    @SerialName("ssm_document_url")
    val ssmDocumentUrl: String? = null
)

@Serializable
data class StoreEditInsertDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val rating: Double,
    @SerialName("contact_phone") val contactPhone: String?,
    @SerialName("opening_time") val openingTime: String?,
    @SerialName("closing_time") val closingTime: String?,
    @SerialName("cleanup_end_time") val cleanupEndTime: String?,
    @SerialName("status") val status: String,
    @SerialName("reason_for_change") val reasonForChange: String? = null
)