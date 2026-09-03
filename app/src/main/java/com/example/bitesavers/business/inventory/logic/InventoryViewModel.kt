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
import com.example.bitesavers.util.DynamicPricingEngine
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

    var defaultPickupStart by mutableStateOf("05:00 PM")
        private set
    var defaultPickupEnd by mutableStateOf("07:00 PM")
        private set

    var defaultCleanupEndTime by mutableStateOf("10:15 PM")
        private set

    init {
        initStoreAndFetchListings()
    }

    private fun initStoreAndFetchListings() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = UserSession.getUserId()
            var matchedStore: StoreDto? = null

            if (userId.isNotBlank()) {
                try {
                    val storeList = SupabaseClient.client.from("stores")
                        .select { filter { eq("owner_id", userId) } }
                        .decodeList<StoreDto>()
                    matchedStore = storeList.firstOrNull()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Fallback store if current owner store not found
            if (matchedStore == null) {
                try {
                    val storeList = SupabaseClient.client.from("stores")
                        .select { limit(1) }
                        .decodeList<StoreDto>()
                    matchedStore = storeList.firstOrNull()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            matchedStore?.let { store ->
                currentStoreId = store.id
                // Generates an automated consumer pickup window based on operating hours
                val (suggestedStart, suggestedEnd) = DynamicPricingEngine.generateDefaultPickupWindow(
                    openingTime24 = store.openingTime,
                    closingTime24 = store.closingTime
                )
                defaultPickupStart = suggestedStart
                defaultPickupEnd = suggestedEnd
                defaultCleanupEndTime = formatToAmPm(store.cleanupEndTime)
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
                            filter { eq("store_id", currentStoreId) }
                        }
                    }
                    .decodeList<OfferDto>()

                // Maps items and formats any SQL 24-hour times into user-friendly 12-hour AM/PM
                _listings.value = result.map { offer ->
                    val item = offer.toListingItem(defaultPickupStart, defaultPickupEnd, defaultCleanupEndTime)
                    item.copy(
                        pickupStart = formatToAmPm(item.pickupStart),
                        pickupEnd = formatToAmPm(item.pickupEnd),
                        cleanupEndTime = formatToAmPm(defaultCleanupEndTime)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

                // Convert 12-hour UI strings to 24-hour Postgres formats before saving
                val formattedStart = DynamicPricingEngine.to24HourTime(localItem.pickupStart) ?: localItem.pickupStart
                val formattedEnd = DynamicPricingEngine.to24HourTime(localItem.pickupEnd) ?: localItem.pickupEnd

                val dtoToInsert = localItem.copy(
                    imageUrl = finalImageUrl,
                    pickupStart = formattedStart,
                    pickupEnd = formattedEnd
                ).toDto(currentStoreId)

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

                val formattedStart = DynamicPricingEngine.to24HourTime(item.pickupStart) ?: item.pickupStart
                val formattedEnd = DynamicPricingEngine.to24HourTime(item.pickupEnd) ?: item.pickupEnd

                val dtoToUpdate = item.copy(
                    imageUrl = finalImageUrl,
                    pickupStart = formattedStart,
                    pickupEnd = formattedEnd
                ).toDto(currentStoreId)

                SupabaseClient.client
                    .from("offers")
                    .update(dtoToUpdate) {
                        filter { eq("id", item.id) }
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
                    .delete { filter { eq("id", itemId) } }
                fetchListings()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Safely converts 24-hour SQL time strings to 12-hour AM/PM strings for display
    private fun formatToAmPm(timeStr: String?): String {
        if (timeStr.isNullOrBlank()) return ""
        val trimmed = timeStr.trim()
        if (trimmed.endsWith("AM", ignoreCase = true) || trimmed.endsWith("PM", ignoreCase = true)) {
            return trimmed
        }
        return try {
            val parser = SimpleDateFormat("HH:mm", Locale.US)
            val formatter = SimpleDateFormat("hh:mm a", Locale.US)
            val date = parser.parse(trimmed.take(5))
            if (date != null) formatter.format(date) else trimmed
        } catch (_: Exception) {
            trimmed
        }
    }
}