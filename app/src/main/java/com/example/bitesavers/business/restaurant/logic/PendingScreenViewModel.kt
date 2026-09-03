package com.example.bitesavers.business.restaurant.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.StoreDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.util.Locale

class PendingScreenViewModel : ViewModel() {
    var restaurantName by mutableStateOf("")
        private set
    var ssmNumber by mutableStateOf("")
        private set
    var contactPhone by mutableStateOf("")
        private set
    var address by mutableStateOf("")
        private set
    var openingTime by mutableStateOf("")
        private set
    var closingTime by mutableStateOf("")
        private set
    var cleanupEndTime by mutableStateOf("")
        private set
    var ssmDocUploaded by mutableStateOf(false)
        private set

    init {
        fetchRestaurantDetails()
    }

    fun fetchRestaurantDetails() {
        viewModelScope.launch {
            try {
                val userId = UserSession.getUserId()
                val stores = SupabaseClient.client
                    .from("stores")
                    .select {
                        filter { eq("owner_id", userId) }
                    }
                    .decodeList<StoreDto>()

                if (stores.isNotEmpty()) {
                    val store = stores.first()
                    restaurantName = store.name ?: ""
                    ssmNumber = store.ssmNumber ?: ""
                    contactPhone = store.contactPhone ?: ""
                    address = store.address ?: ""
                    openingTime = formatTo12Hour(store.openingTime ?: "")
                    closingTime = formatTo12Hour(store.closingTime ?: "")
                    cleanupEndTime = formatTo12Hour(store.cleanupEndTime ?: "")

                    ssmDocUploaded = !store.ssmDocumentUrl.isNullOrBlank()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 👇 Helper function to convert "18:00:00" to "06:00 PM"
    private fun formatTo12Hour(timeStr: String): String {
        if (timeStr.isBlank()) return ""
        if (timeStr.contains("AM", true) || timeStr.contains("PM", true)) return timeStr

        return try {
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                val amPm = if (hour < 12) "AM" else "PM"
                val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)
            } else {
                timeStr
            }
        } catch (e: Exception) {
            timeStr
        }
    }
}