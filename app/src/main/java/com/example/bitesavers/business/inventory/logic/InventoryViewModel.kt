package com.example.bitesavers.business.inventory.logic

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.business.inventory.data.ListingItem
import com.example.bitesavers.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID


class InventoryViewModel : ViewModel() {
    private val _listings = MutableStateFlow<List<ListingItem>>(emptyList())
    val listings: StateFlow<List<ListingItem>> = _listings.asStateFlow()

    // Holds the item being edited (null means creating new food)
    var selectedItemForEdit by mutableStateOf<ListingItem?>(null)

    init {
        fetchListings() // open screen and automatically get the data from supabase
    }

    fun fetchListings() {
        viewModelScope.launch {
            try {
                val result = SupabaseClient.client
                    .from("offers")
                    .select()
                    .decodeList<ListingItem>()
                _listings.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun uploadFoodImage(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val outputStream = ByteArrayOutputStream() //
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
        viewModelScope.launch {
            try {
                var finalImageUrl = item.imageUrl

                // if take photo or choose picture,upload fist in supabase storage
                if (item.imageBitmap != null) {
                    val uploadedUrl = uploadFoodImage(item.imageBitmap)
                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                    }
                }

                val itemToInsert = item.copy(
                    id = if (item.id.isBlank()) UUID.randomUUID().toString() else item.id,
                    imageUrl = finalImageUrl
                )

                SupabaseClient.client
                    .from("offers")
                    .insert(itemToInsert)

                // refresh list and get new data
                fetchListings()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // updateListing
    fun updateListing(item: ListingItem) {
        viewModelScope.launch {
            try {
                var finalImageUrl = item.imageUrl

                // Changed the new image if update
                if (item.imageBitmap != null) {
                    val uploadedUrl = uploadFoodImage(item.imageBitmap)
                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                    }
                }

                val itemToUpdate = item.copy(imageUrl = finalImageUrl)

                SupabaseClient.client
                    .from("offers")
                    .update(itemToUpdate) {
                        filter {
                            eq("id", item.id)
                        }
                    }

                fetchListings()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteListing(itemId: String) {
        viewModelScope.launch {
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
