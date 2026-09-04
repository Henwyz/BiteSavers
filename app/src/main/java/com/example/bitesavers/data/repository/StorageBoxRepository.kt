package com.example.bitesavers.data.repository

import android.util.Log
import com.example.bitesavers.data.dto.StorageBoxDto
import com.example.bitesavers.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageBoxRepository {

    private val client = SupabaseClient.client

    /**
     * Fetch all storage boxes assigned to a specific store
     */
    suspend fun fetchBoxesByStoreId(storeId: String): List<StorageBoxDto> = withContext(Dispatchers.IO) {
        if (storeId.isBlank()) return@withContext emptyList()
        try {
            client.from("storage_boxes")
                .select {
                    filter {
                        eq("store_id", storeId)
                    }
                }
                .decodeList<StorageBoxDto>()
        } catch (e: Exception) {
            Log.e("StorageBoxRepository", "fetchBoxesByStoreId error: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get a specific sensor reading by box ID
     */
    suspend fun fetchBoxById(boxId: String): StorageBoxDto? = withContext(Dispatchers.IO) {
        if (boxId.isBlank()) return@withContext null
        try {
            client.from("storage_boxes")
                .select {
                    filter {
                        eq("id", boxId)
                    }
                }
                .decodeSingleOrNull<StorageBoxDto>()
        } catch (e: Exception) {
            Log.e("StorageBoxRepository", "fetchBoxById error: ${e.message}", e)
            null
        }
    }

    /**
     * Link/Register a sensor to a store using the sensor box code (e.g., 'SB-HOT-001') or ID
     */
    suspend fun claimBox(
        storeId: String,
        sensorCodeInput: String,
        isHotBox: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanCode = sensorCodeInput.trim()
        if (cleanCode.isBlank()) return@withContext Result.failure(IllegalArgumentException("Code is empty"))

        try {
            var candidates = client.from("storage_boxes")
                .select {
                    filter {
                        ilike("box_code", cleanCode)
                    }
                }
                .decodeList<StorageBoxDto>()

            if (candidates.isEmpty()) {
                candidates = client.from("storage_boxes")
                    .select {
                        filter {
                            eq("id", cleanCode)
                        }
                    }
                    .decodeList<StorageBoxDto>()
            }

            if (candidates.isEmpty()) {
                return@withContext Result.failure(NoSuchElementException("Box not found"))
            }

            val targetBox = candidates.first()

            if (!targetBox.storeId.isNullOrBlank() && targetBox.storeId != storeId) {
                return@withContext Result.failure(IllegalStateException("Box already claimed"))
            }

            val chosenType = if (isHotBox) "Hot Box" else "Refrigerator"
            val targetTemp = if (isHotBox) 60.0 else 4.0

            client.from("storage_boxes")
                .update({
                    set("store_id", storeId)
                    set("storage_type", chosenType)
                    set("target_temperature", targetTemp)
                }) {
                    filter {
                        eq("id", targetBox.id)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StorageBoxRepository", "claimBox failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Unlink/Release a storage box from a store
     */
    suspend fun unassignBox(boxId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("storage_boxes")
                .update({
                    set("store_id", null as String?)
                }) {
                    filter {
                        eq("id", boxId)
                    }
                }
            true
        } catch (e: Exception) {
            Log.e("StorageBoxRepository", "unassignBox failed: ${e.message}", e)
            false
        }
    }
}