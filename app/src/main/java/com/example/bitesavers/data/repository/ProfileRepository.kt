package com.example.bitesavers.data.remote.repository

import android.util.Log
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.*
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class ProfileRepository {

    private val postgrest = SupabaseClient.client.postgrest
    private val auth = SupabaseClient.client.auth

    // =================================================================
    // 1. USER OPERATIONS
    // =================================================================
    suspend fun getUser(userId: String): UserDto =
        postgrest.from("users")
            .select { filter { eq("id", userId) } }
            .decodeSingle<UserDto>()

    suspend fun updateUserProfile(userId: String, name: String, email: String) {
        postgrest.from("users")
            .update({
                set("name", name)
                set("email", email)
            }) {
                filter { eq("id", userId) }
            }
    }

    // Updates active user password using Supabase Auth
    suspend fun updateUserPassword(newPassword: String): Result<Unit> {
        return try {
            auth.updateUser {
                password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "updateUserPassword error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =================================================================
    // 2. STORE OPERATIONS (stores table)
    // =================================================================
    suspend fun getStoreRowsByOwnerId(ownerId: String): List<StoreDto> {
        return try {
            postgrest.from("stores")
                .select {
                    filter { eq("owner_id", ownerId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<StoreDto>()
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error fetching stores for owner_id '$ownerId'", e)
            emptyList()
        }
    }

    suspend fun getFirstStoreFallback(): StoreDto? {
        return try {
            postgrest.from("stores")
                .select { limit(1) }
                .decodeList<StoreDto>()
                .firstOrNull()
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error fetching fallback store", e)
            null
        }
    }

    suspend fun insertStoreEditRequest(
        ownerId: String,
        name: String,
        address: String,
        phone: String,
        operatingHours: String,
        cleanupHours: String,
        latitude: Double = 3.1390,
        longitude: Double = 101.6869,
        reasonForChange: String? = null
    ) {
        val times = operatingHours.split("-").map { it.trim() }
        val openingTime = times.getOrNull(0) ?: "08:30"
        val closingTime = times.getOrNull(1) ?: "21:00"

        val insertDto = StoreEditInsertDto(
            id = java.util.UUID.randomUUID().toString(),
            ownerId = ownerId,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            rating = 4.8,
            contactPhone = phone,
            openingTime = openingTime,
            closingTime = closingTime,
            cleanupEndTime = cleanupHours,
            status = "PENDING",
            reasonForChange = reasonForChange
        )

        postgrest.from("stores").insert(insertDto)
        Log.d("ProfileRepository", "Successfully inserted new PENDING store row into 'stores'")
    }

    // =================================================================
    // 3. NGO OPERATIONS
    // =================================================================
    suspend fun getNgoApplications(userId: String): List<NgoApplicationDto> =
        postgrest.from("ngo_applications")
            .select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<NgoApplicationDto>()

    suspend fun insertNgoApplication(dto: NgoApplicationInsertDto) {
        postgrest.from("ngo_applications").insert(dto)
    }

    suspend fun updateUserNgoStatus(userId: String, status: String, orgName: String?) {
        postgrest.from("users")
            .update({
                set("ngo_status", status)
                set("ngo_org_name", orgName)
            }) {
                filter { eq("id", userId) }
            }
    }

    // =================================================================
    // 4. ORDER & ANALYTICS OPERATIONS (orders table)
    // =================================================================
    suspend fun getOrdersByStoreId(storeId: String): List<OrderDto> {
        return try {
            postgrest.from("orders")
                .select {
                    filter { eq("store_id", storeId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<OrderDto>()
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error fetching orders for store_id '$storeId'", e)
            emptyList()
        }
    }
}