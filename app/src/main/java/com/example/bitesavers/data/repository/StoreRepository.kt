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