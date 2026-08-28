package com.example.bitesavers.data.repository

import android.util.Log
import com.example.bitesavers.customer.discovery.data.UserUiModel
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.UserDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    private val client = SupabaseClient.client

    // Fetch the currently active user profile DTO
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

    // Fetch user record and convert to UserUiModel with generated initials
    suspend fun fetchUserProfile(userId: String): UserUiModel? = withContext(Dispatchers.IO) {
        try {
            val user = client.from("users")
                .select { filter { eq("id", userId) } }
                .decodeSingle<UserDto>()

            val initials = user.name
                ?.trim()
                ?.split("\\s+".toRegex())
                ?.filter { it.isNotEmpty() }
                ?.take(2)
                ?.map { it.first().uppercase() }
                ?.joinToString("")
                ?.ifBlank { "ME" } ?: "ME"

            UserUiModel(
                greeting = "👋 Good Evening",
                displayName = user.name ?: "Food Rescuer",
                avatarInitials = initials
            )
        } catch (e: Exception) {
            Log.e("UserRepository", "fetchUserProfile error: ${e.message}", e)
            null
        }
    }

    // Update user stats (e.g. after rescuing food)
    suspend fun incrementMealsRescued(count: Int = 1): Boolean = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            val user = getCurrentUser() ?: return@withContext false
            val currentMeals = user.mealsRescued ?: 0
            val newCount = currentMeals + count
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