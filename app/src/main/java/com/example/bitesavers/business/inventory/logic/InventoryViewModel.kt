package com.example.bitesavers.business.inventory.logic

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.business.inventory.data.ListingItem
import com.example.bitesavers.business.inventory.data.toDto
import com.example.bitesavers.business.inventory.data.toListingItem
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.StoreDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID


class InventoryViewModel : ViewModel() {
    private val _listings = MutableStateFlow<List<ListingItem>>(emptyList())
    val listings: StateFlow<List<ListingItem>> = _listings.asStateFlow()

    var selectedItemForEdit by mutableStateOf<ListingItem?>(null)

    var currentStoreId: String = ""
        private set

    var defaultPickupStart by mutableStateOf("")
        private set
    var defaultPickupEnd by mutableStateOf("")
        private set

    init {
        initStoreAndFetchListings()
    }

    private fun initStoreAndFetchListings() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = UserSession.getUserId()
            if (userId.isNotBlank()) {
                try {
                    val storeList = SupabaseClient.client.from("stores")
                        .select {
                            filter {
                                eq("owner_id", userId)
                            }
                        }
                        .decodeList<StoreDto>()

                    val store = storeList.firstOrNull()
                    if (store != null) {
                        currentStoreId = store.id
                        store.closingTime?.let { defaultPickupStart = formatTo12Hour(it) }
                        store.cleanupEndTime?.let { defaultPickupEnd = formatTo12Hour(it) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Fallback if no store is matched for this user ID
            if (currentStoreId.isBlank()) {
                try {
                    val storeList = SupabaseClient.client.from("stores")
                        .select()
                        .decodeList<StoreDto>()

                    val defaultStore = storeList.firstOrNull()
                    if (defaultStore != null) {
                        currentStoreId = defaultStore.id
                        defaultStore.closingTime?.let { defaultPickupStart = formatTo12Hour(it) }
                        defaultStore.cleanupEndTime?.let { defaultPickupEnd = formatTo12Hour(it) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    currentStoreId = "11111111-1111-1111-1111-111111111111"
                }
            }

            fetchListings()
        }
    }

    fun fetchListings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = SupabaseClient.client
                    .from("offers")
                    .select {
                        if (currentStoreId.isNotBlank()) {
                            filter {
                                eq("store_id", currentStoreId)
                            }
                        }
                    }
                    .decodeList<OfferDto>()

                _listings.value = result.map { it.toListingItem(defaultPickupStart, defaultPickupEnd) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatTo12Hour(time24: String): String {
        return try {
            val parser = SimpleDateFormat("HH:mm:ss", Locale.US)
            val formatter = SimpleDateFormat("hh:mm a", Locale.US)
            val date = parser.parse(time24)
            if (date != null) formatter.format(date) else time24
        } catch (_: Exception) {
            time24
        }
    }

    private suspend fun uploadFoodImage(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()

            val fileName = "food_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val bucket = SupabaseClient.client.storage.from("food_images")

            bucket.upload(fileName, byteArray)
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun addListing(item: ListingItem) {
        val targetId = if (item.id.isBlank()) UUID.randomUUID().toString() else item.id
        val localItem = item.copy(id = targetId, storeId = currentStoreId)

        _listings.value = listOf(localItem) + _listings.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var finalImageUrl = item.imageUrl

                if (item.imageBitmap != null) {
                    val uploadedUrl = uploadFoodImage(item.imageBitmap)
                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                    }
                }

                val dtoToInsert = localItem.copy(imageUrl = finalImageUrl).toDto(currentStoreId)

                SupabaseClient.client
                    .from("offers")
                    .insert(dtoToInsert)

                fetchListings()
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_INSERT_ERROR", "Failed to insert: ${e.message}", e)
            }
        }
    }

    fun updateListing(item: ListingItem) {
        _listings.value = _listings.value.map { if (it.id == item.id) item else it }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var finalImageUrl = item.imageUrl

                if (item.imageBitmap != null) {
                    val uploadedUrl = uploadFoodImage(item.imageBitmap)
                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                    }
                }

                val dtoToUpdate = item.copy(imageUrl = finalImageUrl).toDto(currentStoreId)

                SupabaseClient.client
                    .from("offers")
                    .update(dtoToUpdate) {
                        filter {
                            eq("id", item.id)
                        }
                    }

                fetchListings()
            } catch (e: Exception) {
                android.util.Log.e("SUPABASE_UPDATE_ERROR", "Failed to update: ${e.message}", e)
            }
        }
    }

    fun deleteListing(itemId: String) {
        _listings.value = _listings.value.filterNot { it.id == itemId }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                SupabaseClient.client
                    .from("offers")
                    .delete {
                        filter {
                            eq("id", itemId)
                        }
                    }
                fetchListings()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}