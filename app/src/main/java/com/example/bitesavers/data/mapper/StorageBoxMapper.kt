package com.example.bitesavers.data.mapper

import com.example.bitesavers.data.dto.StorageBoxDto
import com.example.bitesavers.data.model.StorageBox
import com.example.bitesavers.data.model.StorageType

fun StorageBoxDto.toDomain(): StorageBox {
    // Maps raw Supabase strings into binary temperature profiles: HOT_HOLD or CHILLED
    val type = if (storageType?.contains("Hot", ignoreCase = true) == true) {
        StorageType.HOT_HOLD
    } else {
        StorageType.CHILLED
    }

    return StorageBox(
        id = id,
        storeId = storeId,
        boxCode = boxCode,
        storageType = type,
        targetTemperature = targetTemperature,
        currentTemperature = currentTemperature,
        isLocked = isLocked
    )
}