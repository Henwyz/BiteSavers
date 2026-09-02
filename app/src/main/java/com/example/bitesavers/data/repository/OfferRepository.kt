package com.example.bitesavers.data.repository

import com.example.bitesavers.data.mapper.toUiModel
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.StoreDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfferRepository {
    private val client = SupabaseClient.client

    // 1. Discovery Screen: Fetch active inventory with store location data
    suspend fun fetchOffers(): List<OfferUiModel> = withContext(Dispatchers.IO) {
        try {
            val offers = client.from("offers")
                .select {
                    filter {
                        gt("quantity_available", 0)
                        // Matches the status in the seeded database records
                        isIn("status", listOf("AVAILABLE", "ACTIVE"))
                    }
                }
                .decodeList<OfferDto>()

            // Fetch all stores referenced by their clean text IDs (e.g. store_01, store_02)
            val storeIds = offers.map { it.storeId }.distinct()
            val storesMap = if (storeIds.isNotEmpty()) {
                client.from("stores")
                    .select {
                        filter {
                            isIn("id", storeIds)
                        }
                    }
                    .decodeList<StoreDto>()
                    .associateBy { it.id }
            } else {
                emptyMap()
            }

            offers.map { offer ->
                val store = storesMap[offer.storeId]
                offer.toUiModel(store)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 2. Food Detail Screen: Fetch single item with store location data
    suspend fun fetchOfferById(offerId: String): OfferUiModel? = withContext(Dispatchers.IO) {
        try {
            val offer = client.from("offers")
                .select { filter { eq("id", offerId) } }
                .decodeSingle<OfferDto>()

            val store = try {
                client.from("stores")
                    .select { filter { eq("id", offer.storeId) } }
                    .decodeSingle<StoreDto>()
            } catch (_: Exception) {
                null
            }

            offer.toUiModel(store)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Fetches bookmarked items regardless of whether quantity is 0 (to display SOLD OUT state)
    suspend fun fetchSavedOffersByIds(offerIds: Set<String>): List<OfferUiModel> = withContext(Dispatchers.IO) {
        if (offerIds.isEmpty()) return@withContext emptyList()

        try {
            val offers = client.from("offers")
                .select {
                    filter {
                        isIn("id", offerIds.toList())
                    }
                }
                .decodeList<OfferDto>()

            val storeIds = offers.map { it.storeId }.distinct()
            val storesMap = if (storeIds.isNotEmpty()) {
                client.from("stores")
                    .select {
                        filter {
                            isIn("id", storeIds)
                        }
                    }
                    .decodeList<StoreDto>()
                    .associateBy { it.id }
            } else {
                emptyMap()
            }

            offers.map { offer ->
                val store = storesMap[offer.storeId]
                offer.toUiModel(store)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}