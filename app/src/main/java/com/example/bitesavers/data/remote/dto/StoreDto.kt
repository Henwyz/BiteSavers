package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreDto(
    @SerialName("id")
    val id: String = "",

    @SerialName("owner_id")
    val ownerId: String? = null,

    @SerialName("name")
    val name: String,

    @SerialName("address")
    val address: String,

    @SerialName("latitude")
    val latitude: Double = 5.4674,

    @SerialName("longitude")
    val longitude: Double = 100.2790,

    @SerialName("opening_time")
    val openingTime: String, // e.g. "08:00:00"

    @SerialName("closing_time")
    val closingTime: String, // e.g. "21:30:00"

    @SerialName("cleanup_end_time")
    val cleanupEndTime: String = "22:15:00", // e.g. "22:15:00"

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("rating")
    val rating: Double? = null,

    @SerialName("contact_phone")
    val contactPhone: String? = null,

    @SerialName("status")
    val status: String? = "PENDING", // PENDING, APPROVED, REJECTED

    @SerialName("reason_for_change")
    val reasonForChange: String? = null,

    @SerialName("ssm_number")
    val ssmNumber: String? = null,

    @SerialName("ssm_document_url")
    val ssmDocumentUrl: String? = null
)