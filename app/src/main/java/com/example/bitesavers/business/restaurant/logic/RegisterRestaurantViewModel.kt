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
    var cleanupEndTime by mutableStateOf("10:30 PM")
        private set

    // Validation error states
    var cleanupTimeError by mutableStateOf<String?>(null)
        private set
    var ssmError by mutableStateOf<String?>(null)
        private set
    var restaurantNameError by mutableStateOf<String?>(null)
        private set
    var contactPhoneError by mutableStateOf<String?>(null)
        private set
    var addressError by mutableStateOf<String?>(null)
        private set
    var storeImageError by mutableStateOf<String?>(null)
        private set
    var ssmDocError by mutableStateOf<String?>(null)
        private set

    var storeImageUri by mutableStateOf<Uri?>(null)
        private set
    var ssmDocUri by mutableStateOf<Uri?>(null)
        private set

    fun updateRestaurantName(value: String) {
        restaurantName = value
        restaurantNameError = if (value.isBlank()) "Restaurant name is required" else null
    }

    fun updateAddress(value: String) {
        address = value
        addressError = if (value.isBlank()) "Address is required" else null
    }

    fun updateContactPhone(value: String) {
        contactPhone = value
        contactPhoneError = if (value.isBlank()) "Contact phone is required" else null
    }

    fun updateSsmNumber(value: String) {
        if (value.all { it.isDigit() } && value.length <= 12) {
            ssmNumber = value
            ssmError = if (value.length != 12) {
                "SSM number must be exactly 12 digits"
            } else {
                null
            }
        }
    }

    fun updateOpeningTime(value: String) { openingTime = value }

    fun updateClosingTime(value: String) {
        closingTime = value
        validateTimes()
    }

    fun updateCleanupEndTime(value: String) {
        cleanupEndTime = value
        validateTimes()
    }

    private fun validateTimes() {
        if (!RegisterRestaurantValidation.isCleanupValid(closingTime, cleanupEndTime)) {
            cleanupTimeError = "Cleanup time must be after closing time"
        } else {
            cleanupTimeError = null
        }
    }

    fun updateStoreImageUri(uri: Uri?) {
        storeImageUri = uri
        storeImageError = if (uri == null) "Store photo is required" else null
    }

    fun updateSsmDocUri(uri: Uri?) {
        ssmDocUri = uri
        ssmDocError = if (uri == null) "SSM document is required" else null
    }

    fun useDefaultPenangLocation() {
        address = "Penang, Malaysia"
        addressError = null
    }

    private suspend fun uploadFileToStorage(uri: Uri, bucketName: String, prefix: String): String? = withContext(Dispatchers.IO) {
        try {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            val bucket = SupabaseClient.client.storage.from(bucketName)

            bucket.upload(fileName, bytes)
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun registerRestaurant(onSuccess: () -> Unit) {
        // Final sanity check validation on submission trigger
        restaurantNameError = if (restaurantName.isBlank()) "Restaurant name is required" else null
        addressError = if (address.isBlank()) "Address is required" else null
        contactPhoneError = if (contactPhone.isBlank()) "Contact phone is required" else null
        ssmError = if (!RegisterRestaurantValidation.isSsmValid(ssmNumber)) "SSM number must be exactly 12 digits" else null
        storeImageError = if (storeImageUri == null) "Store photo is required" else null
        ssmDocError = if (ssmDocUri == null) "SSM document is required" else null

        if (restaurantNameError != null || addressError != null || contactPhoneError != null ||
            ssmError != null || storeImageError != null || ssmDocError != null || cleanupTimeError != null) {
            return
        }

        viewModelScope.launch {
            try {
                val (resolvedLat, resolvedLng) = LocationUtils.getCoordinatesFromAddress(
                    context = getApplication(),
                    address = address
                )

                val imageUrlStr = uploadFileToStorage(storeImageUri!!, "store-images", "store")
                val ssmDocUrlStr = uploadFileToStorage(ssmDocUri!!, "ssm_documents", "ssm")

                val existingStores = SupabaseClient.client
                    .from("stores")
                    .select()
                    .decodeList<StoreDto>()

                val nextNumber = existingStores.size + 1
                val storeId = String.format("STORE%03d", nextNumber)
                val currentUserId = UserSession.getUserId()

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
                    status = "PENDING"
                )

                SupabaseClient.client
                    .from("stores")
                    .insert(newStore)

                UserSession.setStoreStatus("PENDING")

                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}