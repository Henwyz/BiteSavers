package com.example.bitesavers.data.model

enum class StorageType(val displayName: String) {
    HOT_HOLD("Hot Holding Warmer"),
    CHILLED("Chiller / Fridge")
}

data class StorageBox(
    val id: String,
    val storeId: String?,
    val boxCode: String,
    val storageType: StorageType,
    val targetTemperature: Double?,
    val currentTemperature: Double,
    val isLocked: Boolean
)