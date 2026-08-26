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
                        eq("status", "ACTIVE")
                    }
                }
                .decodeList<OfferDto>()

            // Fetch all stores referenced in these offers
            val storeIds = offers.mapNotNull { it.storeId }.distinct()
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
                val store = offer.storeId?.let { storesMap[it] }
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

            val store = offer.storeId?.let { sId ->
                try {
                    client.from("stores")
                        .select { filter { eq("id", sId) } }
                        .decodeSingle<StoreDto>()
                } catch (_: Exception) {
                    null
                }
            }

            offer.toUiModel(store)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}