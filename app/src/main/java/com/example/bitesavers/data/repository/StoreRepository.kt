package com.example.bitesavers.data.repository

import android.util.Log
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.StoreDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoreRepository {
    private val client = SupabaseClient.client

    // Generates clean, human-readable store ID matching Supabase seed format (e.g. store_172530)
    private fun generateStoreId(): String = "store_${System.currentTimeMillis().toString().takeLast(6)}"

    // Fetch store profile by its primary key ID and resolves the latest approved store details
    suspend fun getStoreById(storeId: String): StoreDto? = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch the target store record to find its owner_id
            val targetStore = client.from("stores")
                .select { filter { eq("id", storeId) } }
                .decodeSingleOrNull<StoreDto>()

            val ownerId = targetStore?.ownerId

            // 2. If owner_id exists, fetch all store rows for this owner sorted newest-first
            if (!ownerId.isNullOrBlank()) {
                val allRows = client.from("stores")
                    .select {
                        filter { eq("owner_id", ownerId) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<StoreDto>()

                // 3. Resolve the latest APPROVED store row containing the freshest details (phone, address, hours)
                allRows.firstOrNull { it.status?.equals("APPROVED", ignoreCase = true) == true }
                    ?: allRows.firstOrNull { it.status?.equals("PENDING", ignoreCase = true) != true }
                    ?: targetStore
            } else {
                targetStore
            }
        } catch (e: Exception) {
            Log.e("StoreRepository", "getStoreById error: ${e.message}", e)
            null
        }
    }

    // Fetches the store owned by a specific merchant user ID, returning the latest approved record
    suspend fun getStoreByOwnerId(ownerId: String): StoreDto? = withContext(Dispatchers.IO) {
        try {
            val list = client.from("stores")
                .select {
                    filter { eq("owner_id", ownerId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<StoreDto>()

            // Resolves the latest APPROVED store record for this owner
            list.firstOrNull { it.status?.equals("APPROVED", ignoreCase = true) == true }
                ?: list.firstOrNull { it.status?.equals("PENDING", ignoreCase = true) != true }
                ?: list.firstOrNull()
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