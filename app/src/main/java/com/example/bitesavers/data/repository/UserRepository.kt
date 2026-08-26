package com.example.bitesavers.data.repository

import android.util.Log
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.UserDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    private val client = SupabaseClient.client

    // Fetch the currently active user profile
    suspend fun getCurrentUser(): UserDto? = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            client.from("users")
                .select { filter { eq("id", uid) } }
                .decodeSingle<UserDto>()
        } catch (e: Exception) {
            Log.e("UserRepository", "getCurrentUser error: ${e.message}", e)
            null
        }
    }

    // Update user stats (e.g. after rescuing food)
    suspend fun incrementMealsRescued(count: Int = 1): Boolean = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            val user = getCurrentUser() ?: return@withContext false
            val newCount = user.mealsRescued + count
            client.from("users")
                .update({ set("meals_rescued", newCount) }) {
                    filter { eq("id", uid) }
                }
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "incrementMealsRescued error: ${e.message}", e)
            false
        }
    }
}