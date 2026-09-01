package com.example.bitesavers.data.repository

import com.example.bitesavers.data.dto.StorageBoxDto
import com.example.bitesavers.data.mapper.toDomain
import com.example.bitesavers.data.model.StorageBox
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageBoxRepository(private val supabase: SupabaseClient) {

    /**
     * Fetch all sensors assigned to a specific store
     */
    suspend fun getBoxesByStoreId(storeId: String): Result<List<StorageBox>> = withContext(Dispatchers.IO) {
        runCatching {
            val dtoList = supabase.from("storage_boxes")
                .select {
                    filter {
                        eq("store_id", storeId)
                    }
                }
                .decodeList<StorageBoxDto>()
            dtoList.map { it.toDomain() }
        }
    }

    /**
     * Link/Register a sensor to a store using the sensor box code (e.g., 'SB-HOT-001')
     */
    suspend fun claimBoxByCode(boxCode: String, storeId: String): Result<StorageBox> = withContext(Dispatchers.IO) {
        runCatching {
            // Check if box exists and is unassigned
            val availableBox = supabase.from("storage_boxes")
                .select {
                    filter {
                        eq("box_code", boxCode.trim().uppercase())
                        exact("store_id", null)
                    }
                }
                .decodeSingleOrNull<StorageBoxDto>()
                ?: throw IllegalStateException("Sensor code not found or already registered to another store.")

            // Assign to merchant's store
            val updatedDto = supabase.from("storage_boxes")
                .update(
                    {
                        set("store_id", storeId)
                    }
                ) {
                    filter {
                        eq("id", availableBox.id)
                    }
                    select()
                }
                .decodeSingle<StorageBoxDto>()

            updatedDto.toDomain()
        }
    }

    /**
     * Get a specific sensor reading by box ID (useful for offer detail pages)
     */
    suspend fun getBoxById(boxId: String): Result<StorageBox?> = withContext(Dispatchers.IO) {
        runCatching {
            supabase.from("storage_boxes")
                .select {
                    filter {
                        eq("id", boxId)
                    }
                }
                .decodeSingleOrNull<StorageBoxDto>()?.toDomain()
        }
    }
}