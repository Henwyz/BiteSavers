package com.example.bitesavers.data.repository

import com.example.bitesavers.data.dto.StorageBoxDto
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
                        isIn("status", listOf("AVAILABLE", "ACTIVE"))
                    }
                }
                .decodeList<OfferDto>()

            val storeIds: List<String> = offers.mapNotNull { it.storeId }.distinct()
            val storesMap = if (storeIds.isNotEmpty()) {
                val rawStores = client.from("stores")
                    .select {
                        filter {
                            isIn("id", storeIds)
                        }
                    }
                    .decodeList<StoreDto>()

                // Resolves latest store record per owner so edited store names and hours stay in sync
                val ownerIds = rawStores.mapNotNull { it.ownerId }.distinct()
                val latestStoresByOwner = if (ownerIds.isNotEmpty()) {
                    client.from("stores")
                        .select {
                            filter {
                                isIn("owner_id", ownerIds)
                            }
                        }
                        .decodeList<StoreDto>()
                        .groupBy { it.ownerId }
                        .mapValues { entry ->
                            entry.value.firstOrNull { it.status.equals("APPROVED", ignoreCase = true) }
                                ?: entry.value.lastOrNull()
                        }
                } else emptyMap()

                rawStores.associateBy(
                    keySelector = { it.id },
                    valueTransform = { store ->
                        store.ownerId?.let { latestStoresByOwner[it] } ?: store
                    }
                )
            } else {
                emptyMap()
            }

            // Batch fetch storage boxes for live temperatures
            val boxIds: List<String> = offers.mapNotNull { it.storageBoxId }.distinct()
            val boxesMap = if (boxIds.isNotEmpty()) {
                client.from("storage_boxes")
                    .select {
                        filter {
                            isIn("id", boxIds)
                        }
                    }
                    .decodeList<StorageBoxDto>()
                    .associateBy { it.id }
            } else {
                emptyMap()
            }

            offers.map { offer ->
                val store = offer.storeId?.let { storesMap[it] }
                val box = offer.storageBoxId?.let { boxesMap[it] }
                offer.toUiModel(store = store, storageBox = box)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 2. Food Detail Screen: Fetch single item with real store & storage box telemetry
    suspend fun fetchOfferById(offerId: String): OfferUiModel? = withContext(Dispatchers.IO) {
        try {
            val offer = client.from("offers")
                .select { filter { eq("id", offerId) } }
                .decodeSingle<OfferDto>()

            // Fetch Store for Location & Rating, resolving latest edited store by owner_id
            val targetStoreId = offer.storeId
            val store = if (!targetStoreId.isNullOrBlank()) {
                try {
                    val initialStore = client.from("stores")
                        .select { filter { eq("id", targetStoreId) } }
                        .decodeSingle<StoreDto>()

                    if (!initialStore.ownerId.isNullOrBlank()) {
                        val ownerStores = client.from("stores")
                            .select { filter { eq("owner_id", initialStore.ownerId) } }
                            .decodeList<StoreDto>()

                        ownerStores.firstOrNull { it.status.equals("APPROVED", ignoreCase = true) }
                            ?: ownerStores.lastOrNull()
                            ?: initialStore
                    } else {
                        initialStore
                    }
                } catch (_: Exception) {
                    null
                }
            } else null

            // Fetch Connected IoT Storage Box for Live Temperature
            val targetBoxId = offer.storageBoxId
            val storageBox = if (!targetBoxId.isNullOrBlank()) {
                try {
                    client.from("storage_boxes")
                        .select { filter { eq("id", targetBoxId) } }
                        .decodeSingle<StorageBoxDto>()
                } catch (_: Exception) {
                    null
                }
            } else null

            offer.toUiModel(store = store, storageBox = storageBox)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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

            val storeIds: List<String> = offers.mapNotNull { it.storeId }.distinct()
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

            val boxIds: List<String> = offers.mapNotNull { it.storageBoxId }.distinct()
            val boxesMap = if (boxIds.isNotEmpty()) {
                client.from("storage_boxes")
                    .select {
                        filter {
                            isIn("id", boxIds)
                        }
                    }
                    .decodeList<StorageBoxDto>()
                    .associateBy { it.id }
            } else {
                emptyMap()
            }

            offers.map { offer ->
                val store = offer.storeId?.let { storesMap[it] }
                val box = offer.storageBoxId?.let { boxesMap[it] }
                offer.toUiModel(store = store, storageBox = box)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}