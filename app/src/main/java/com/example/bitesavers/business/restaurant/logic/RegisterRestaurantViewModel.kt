package com.example.bitesavers.business.restaurant.logic

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.R
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

    // Loading state to prevent duplicate submissions on double-tap
    var isLoading by mutableStateOf(false)
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
        val context = getApplication<Application>()
        restaurantNameError = if (value.isBlank()) context.getString(R.string.error_restaurant_name_required) else null
    }

    fun updateAddress(value: String) {
        address = value
        val context = getApplication<Application>()
        addressError = if (value.isBlank()) context.getString(R.string.error_address_required) else null
    }

    fun updateContactPhone(value: String) {
        contactPhone = value
        val context = getApplication<Application>()
        contactPhoneError = if (value.isBlank()) context.getString(R.string.error_contact_phone_required) else null
    }

    fun updateSsmNumber(value: String) {
        val context = getApplication<Application>()
        if (value.all { it.isDigit() } && value.length <= 12) {
            ssmNumber = value
            ssmError = if (value.length != 12) {
                context.getString(R.string.error_ssm_invalid)
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
        val context = getApplication<Application>()
        if (!RegisterRestaurantValidation.isCleanupValid(closingTime, cleanupEndTime)) {
            cleanupTimeError = context.getString(R.string.error_cleanup_time_invalid)
        } else {
            cleanupTimeError = null
        }
    }

    fun updateStoreImageUri(uri: Uri?) {
        storeImageUri = uri
        val context = getApplication<Application>()
        storeImageError = if (uri == null) context.getString(R.string.error_store_photo_required) else null
    }

    fun updateSsmDocUri(uri: Uri?) {
        ssmDocUri = uri
        val context = getApplication<Application>()
        ssmDocError = if (uri == null) context.getString(R.string.error_ssm_doc_required) else null
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
        if (isLoading) return // Prevents multiple rapid clicks

        // Final sanity check validation on submission trigger using string resources
        val context = getApplication<Application>()
        restaurantNameError = if (restaurantName.isBlank()) context.getString(R.string.error_restaurant_name_required) else null
        addressError = if (address.isBlank()) context.getString(R.string.error_address_required) else null
        contactPhoneError = if (contactPhone.isBlank()) context.getString(R.string.error_contact_phone_required) else null
        ssmError = if (!RegisterRestaurantValidation.isSsmValid(ssmNumber)) context.getString(R.string.error_ssm_invalid) else null
        storeImageError = if (storeImageUri == null) context.getString(R.string.error_store_photo_required) else null
        ssmDocError = if (ssmDocUri == null) context.getString(R.string.error_ssm_doc_required) else null

        if (restaurantNameError != null || addressError != null || contactPhoneError != null ||
            ssmError != null || storeImageError != null || ssmDocError != null || cleanupTimeError != null) {
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val (resolvedLat, resolvedLng) = LocationUtils.getCoordinatesFromAddress(
                    context = getApplication(),
                    address = address
                )

                // Validates if address is resolvable via Geocoder and blocks submission if it fails lookup
                if (resolvedLat == 5.4674 && resolvedLng == 100.2790 && !address.contains("Penang", ignoreCase = true)) {
                    addressError = context.getString(R.string.error_invalid_address)
                    isLoading = false
                    return@launch
                }

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
            } finally {
                isLoading = false
            }
        }
    }
}