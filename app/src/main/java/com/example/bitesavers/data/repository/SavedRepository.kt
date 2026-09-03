package com.example.bitesavers.data.repository

import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.SavedOfferDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class SavedRepository {
    private val client = SupabaseClient.client

    companion object {
        // In-memory set of saved offer IDs for reactive UI updates across screens
        private val _savedOfferIds = MutableStateFlow<Set<String>>(emptySet())
        val savedOfferIds: StateFlow<Set<String>> = _savedOfferIds.asStateFlow()
    }

    // Generates human-readable ID matching Supabase seed format (e.g., save_1001 or save_timestamp)
    private fun generateSaveId(): String = "save_${System.currentTimeMillis().toString().takeLast(6)}"

    suspend fun loadUserSavedOffers(userId: String) = withContext(Dispatchers.IO) {
        try {
            val list = client.from("saved_offers")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<SavedOfferDto>()
            _savedOfferIds.value = list.map { it.offerId }.toSet()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun toggleSaveOffer(userId: String, offerId: String) = withContext(Dispatchers.IO) {
        val isCurrentlySaved = _savedOfferIds.value.contains(offerId)

        // Optimistic local update
        _savedOfferIds.update { current ->
            if (isCurrentlySaved) current - offerId else current + offerId
        }

        try {
            if (isCurrentlySaved) {
                client.from("saved_offers").delete {
                    filter {
                        eq("user_id", userId)
                        eq("offer_id", offerId)
                    }
                }
            } else {
                // Inserts clean readable primary key
                client.from("saved_offers").insert(
                    SavedOfferDto(
                        id = generateSaveId(),
                        userId = userId,
                        offerId = offerId
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Revert state if the network call fails
            _savedOfferIds.update { current ->
                if (isCurrentlySaved) current + offerId else current - offerId
            }
        }
    }

    fun isOfferSaved(offerId: String): Boolean {
        return _savedOfferIds.value.contains(offerId)
    }
}