package com.example.bitesavers.business.restaurant.logic

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.StoreDto
import com.example.bitesavers.util.LocationUtils
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class RegisterRestaurantViewModel(application: Application) : AndroidViewModel(application) {
    var restaurantName by mutableStateOf("")
        private set
    var contactPhone by mutableStateOf("")
        private set
    var address by mutableStateOf("")
        private set
    var ssmNumber by mutableStateOf("")
        private set
    var openingTime by mutableStateOf("09:00 AM")
        private set
    var closingTime by mutableStateOf("10:00 PM")
        private set
    var cleanupEndTime by mutableStateOf("22:30")
        private set

    var storeImageUri by mutableStateOf<Uri?>(null)
        private set
    var ssmDocUri by mutableStateOf<Uri?>(null)
        private set

    fun updateRestaurantName(value: String) { restaurantName = value }
    fun updateAddress(value: String) { address = value }
    fun updateContactPhone(value: String) { contactPhone = value }
    fun updateSsmNumber(value: String) { ssmNumber = value }
    fun updateOpeningTime(value: String) { openingTime = value }
    fun updateClosingTime(value: String) { closingTime = value }
    fun updateCleanupEndTime(value: String) { cleanupEndTime = value }
    fun updateStoreImageUri(uri: Uri?) { storeImageUri = uri }
    fun updateSsmDocUri(uri: Uri?) { ssmDocUri = uri }

    fun useDefaultPenangLocation() {
        address = "Penang, Malaysia"
    }

    private suspend fun uploadFileToStorage(uri: Uri, bucketName: String, prefix: String): String? = withContext(Dispatchers.IO) {
        try {
            // Get application the context to access the device's content resolver
            val context = getApplication<Application>().applicationContext

            // Open an input stream from the given file URI (gallery photo or SSM document)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()

            // Generate a unique file name using a prefix (e.g., store_ or ssm_), current time, and a random hash
            val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"

            // Connect to the designated Supabase storage bucket
            val bucket = SupabaseClient.client.storage.from(bucketName)

            // 7. Upload the raw byte array into the Supabase bucket and return its public URL
            bucket.upload(fileName, bytes)
            bucket.publicUrl(fileName)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun registerRestaurant(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Resolve coordinates using LocationUtils with Penang fallback[cite: 2]
                val (resolvedLat, resolvedLng) = LocationUtils.getCoordinatesFromAddress(
                    context = getApplication(),
                    address = address
                )

                // 2. Upload store image & SSM document files to Supabase buckets
                var imageUrlStr: String? = null
                if (storeImageUri != null) {
                    imageUrlStr = uploadFileToStorage(storeImageUri!!, "store-images", "store")
                }

                var ssmDocUrlStr: String? = null
                if (ssmDocUri != null) {
                    ssmDocUrlStr = uploadFileToStorage(ssmDocUri!!, "ssm_documents", "ssm")
                }

                val existingStores = SupabaseClient.client
                    .from("stores")
                    .select()
                    .decodeList<StoreDto>()

                val nextNumber = existingStores.size + 1
                val storeId = String.format("STORE%03d", nextNumber)
                val currentUserId = UserSession.getUserId()

                // 3. Map into the shared StoreDto structure
                val newStore = StoreDto(
                    id = storeId,
                    name = restaurantName.trim(),
                    address = address.trim(),
                    latitude = resolvedLat,
                    longitude = resolvedLng,
                    rating = 4.8,
                    contactPhone = contactPhone.trim(),
                    openingTime = openingTime,
                    closingTime = closingTime,
                    cleanupEndTime = cleanupEndTime,
                    imageUrl = imageUrlStr,
                    ownerId = currentUserId,
                    ssmNumber = ssmNumber.trim(),
                    ssmDocumentUrl = ssmDocUrlStr,
                    status = "PENDING" // Default status for new registrations
                )

                // 4. Save to Supabase 'stores' table
                SupabaseClient.client
                    .from("stores")
                    .insert(newStore)

                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}