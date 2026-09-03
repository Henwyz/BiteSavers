package com.example.bitesavers.data.repository

import android.util.Log
import com.example.bitesavers.customer.discovery.data.UserUiModel
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.UserDto
import io.github.jan.supabase.gotrue.auth
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

    // Evaluates whether the user's NGO status is explicitly APPROVED in Supabase
    suspend fun fetchUserNgoStatus(userId: String): String = withContext(Dispatchers.IO) {
        try {
            val user = client.from("users")
                .select { filter { eq("id", userId) } }
                .decodeSingle<UserDto>()
            user.ngoStatus
        } catch (e: Exception) {
            Log.e("UserRepository", "fetchUserNgoStatus error: ${e.message}", e)
            "NONE"
        }
    }

    // Fetch user record and convert to UserUiModel with generated initials
    suspend fun fetchUserProfile(userId: String): UserUiModel? = withContext(Dispatchers.IO) {
        try {
            val user = client.from("users")
                .select { filter { eq("id", userId) } }
                .decodeSingle<UserDto>()

            val initials = user.name
                .trim()
                .split("\\s+".toRegex())
                .filter { it.isNotEmpty() }
                .take(2)
                .map { it.first().uppercase() }
                .joinToString("")
                .ifBlank { "ME" }

            UserUiModel(
                greeting = "👋 Good Evening",
                displayName = user.name.ifBlank { "Food Rescuer" },
                avatarInitials = initials
            )
        } catch (e: Exception) {
            Log.e("UserRepository", "fetchUserProfile error: ${e.message}", e)
            null
        }
    }

    // Update user profile display name in Supabase
    suspend fun updateUserName(userId: String, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("users")
                .update({ set("name", newName) }) {
                    filter { eq("id", userId) }
                }
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "updateUserName error: ${e.message}", e)
            false
        }
    }

    // Signs out user, wipes persistent session from disk, and invalidates Supabase auth
    suspend fun signOut() {
        try {
            SupabaseClient.client.auth.signOut()
        } catch (e: Exception) {
            Log.e("UserRepository", "signOut error: ${e.message}", e)
        } finally {
            UserSession.clear()
        }
    }
}