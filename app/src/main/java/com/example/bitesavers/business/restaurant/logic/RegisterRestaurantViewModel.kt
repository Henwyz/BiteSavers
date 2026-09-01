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

class RegisterRestaurantViewModel : ViewModel() {
    var restaurantName by mutableStateOf("")
        private set
    var address by mutableStateOf("")
        private set
    var openingTime by mutableStateOf("09:00 AM")
        private set
    var closingTime by mutableStateOf("10:00 PM")
        private set

    fun updateRestaurantName(value: String) { restaurantName = value }
    fun updateAddress(value: String) { address = value }
    fun updateOpeningTime(value: String) { openingTime = value }
    fun updateClosingTime(value: String) { closingTime = value }

    fun registerRestaurant(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val existingStores = SupabaseClient.client
                .from("stores")
                .select()
                .decodeList<StoreDto>()

            // Generate sequential ID like STORE001, STORE002...
            val nextNumber = existingStores.size + 1
            val storeId = String.format("STORE%03d", nextNumber)

            // Get the current logged-in user's ID to store in owner_id column
            val currentUserId = UserSession.getUserId()

            val newStore = StoreDto(
                id = storeId,
                name = restaurantName.trim(),
                address = address.trim(),
                openingTime = openingTime,
                closingTime = closingTime,
                ownerId = currentUserId
            )

            SupabaseClient.client
                .from("stores")
                .insert(newStore)

            onSuccess()
        }
    }
}