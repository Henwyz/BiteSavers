package com.example.bitesavers.data.repository

import com.example.bitesavers.data.mapper.toUiModel
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.OfferDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfferRepository {
    private val client = SupabaseClient.client

    // 1. Discovery Screen
    suspend fun fetchOffers(): List<OfferUiModel> = withContext(Dispatchers.IO) {
        try {
            val dtoList = client.from("offers")
                .select { filter { gt("quantity_left", 0) } }
                .decodeList<OfferDto>()
            dtoList.map { it.toUiModel() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 2. Food Detail Screen
    suspend fun fetchOfferById(offerId: String): OfferUiModel? = withContext(Dispatchers.IO) {
        try {
            val dto = client.from("offers")
                .select { filter { eq("id", offerId) } }
                .decodeSingle<OfferDto>()
            dto.toUiModel()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}