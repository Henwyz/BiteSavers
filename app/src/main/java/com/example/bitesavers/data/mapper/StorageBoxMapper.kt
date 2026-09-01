package com.example.bitesavers.data.mapper

import com.example.bitesavers.data.dto.StorageBoxDto
import com.example.bitesavers.data.model.StorageBox
import com.example.bitesavers.data.model.StorageType

fun StorageBoxDto.toDomain(): StorageBox {
    val type = try {
        StorageType.valueOf(storageType.uppercase())
    } catch (e: Exception) {
        StorageType.ROOM_TEMP
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