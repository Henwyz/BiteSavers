package com.example.bitesavers.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StorageBoxDto(
    @SerialName("id") val id: String,
    @SerialName("store_id") val storeId: String? = null,
    @SerialName("box_code") val boxCode: String,
    @SerialName("storage_type") val storageType: String,
    @SerialName("target_temperature") val targetTemperature: Double,
    @SerialName("current_temperature") val currentTemperature: Double,
    @SerialName("is_locked") val isLocked: Boolean = true,
    @SerialName("last_synced_at") val lastSyncedAt: String? = null
)