package com.example.bitesavers.data.repository

import android.util.Log
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.StoreDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoreRepository {
    private val client = SupabaseClient.client

    // Generates clean, human-readable store ID matching Supabase seed format (e.g. store_172530)
    private fun generateStoreId(): String = "store_${System.currentTimeMillis().toString().takeLast(6)}"

    // Fetch store profile by its primary key ID
    suspend fun getStoreById(storeId: String): StoreDto? = withContext(Dispatchers.IO) {
        try {
            client.from("stores")
                .select { filter { eq("id", storeId) } }
                .decodeSingle<StoreDto>()
        } catch (e: Exception) {
            Log.e("StoreRepository", "getStoreById error: ${e.message}", e)
            null
        }
    }

    // Fetches the store owned by a specific merchant user ID (enforces 1:1 owner to store relationship)
    suspend fun getStoreByOwnerId(ownerId: String): StoreDto? = withContext(Dispatchers.IO) {
        try {
            val list = client.from("stores")
                .select { filter { eq("owner_id", ownerId) } }
                .decodeList<StoreDto>()
            list.firstOrNull()
        } catch (e: Exception) {
            Log.e("StoreRepository", "getStoreByOwnerId error: ${e.message}", e)
            null
        }
    }

    // Creates a new store record with standardized store_ prefix ID linked to merchant owner_id
    suspend fun createStore(store: StoreDto): StoreDto? = withContext(Dispatchers.IO) {
        try {
            val storeWithCleanId = if (store.id.isBlank()) {
                store.copy(id = generateStoreId())
            } else {
                store
            }

            client.from("stores")
                .insert(storeWithCleanId) { select() }
                .decodeSingle<StoreDto>()
        } catch (e: Exception) {
            Log.e("StoreRepository", "createStore error: ${e.message}", e)
            null
        }
    }

    // Fetch all active offers published by this specific store
    suspend fun getOffersByStoreId(storeId: String): List<OfferDto> = withContext(Dispatchers.IO) {
        try {
            client.from("offers")
                .select {
                    filter {
                        eq("store_id", storeId)
                        gt("quantity_available", 0)
                    }
                }
                .decodeList<OfferDto>()
        } catch (e: Exception) {
            Log.e("StoreRepository", "getOffersByStoreId error: ${e.message}", e)
            emptyList()
        }
    }
}