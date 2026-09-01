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
import okhttp3.Dispatcher
import java.io.ByteArrayOutputStream
import java.util.UUID


class InventoryViewModel : ViewModel() {
    private val _listings = MutableStateFlow<List<ListingItem>>(emptyList())
    val listings: StateFlow<List<ListingItem>> = _listings.asStateFlow()

    var selectedItemForEdit by mutableStateOf<ListingItem?>(null)

    init {
        fetchListings()
    }

    fun fetchListings() {
        viewModelScope.launch(Dispatchers.IO) {
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
        _listings.value = listOf(item) + _listings.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var finalImageUrl = item.imageUrl

                if (item.imageBitmap != null) {
                    val uploadedUrl = uploadFoodImage(item.imageBitmap)
                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                    }
                }

                val itemToInsert = item.copy(
                    id = if (item.id.isBlank()) UUID.randomUUID().toString() else item.id,
                    imageUrl = finalImageUrl,
                    imageBitmap = null
                )

                SupabaseClient.client
                    .from("offers")
                    .insert(itemToInsert)

                fetchListings()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateListing(item: ListingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var finalImageUrl = item.imageUrl

                if (item.imageBitmap != null) {
                    val uploadedUrl = uploadFoodImage(item.imageBitmap)
                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                    }
                }

                val itemToUpdate = item.copy(
                    imageUrl = finalImageUrl,
                    imageBitmap = null
                )

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