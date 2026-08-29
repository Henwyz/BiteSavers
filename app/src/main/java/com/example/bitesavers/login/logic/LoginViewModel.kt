package com.example.bitesavers.login.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.UserDto
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var selectedRole by mutableStateOf("Consumer")
        private set
    var isDropdownExpanded by mutableStateOf(false)
        private set
    var passwordVisible by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun updateEmail(value: String) {
        email = value
        errorMessage = null
    }
    fun updatePassword(value: String) {
        password = value
        errorMessage = null
    }
    fun updateSelectedRole(value: String) { selectedRole = value }
    fun toggleDropdown(expanded: Boolean) { isDropdownExpanded = expanded }
    fun togglePasswordVisibility() { passwordVisible = !passwordVisible }

    fun login(onSuccess: (isBusiness: Boolean) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both email and password"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Sends credentials to Supabase Auth to decrypt and verify the password hash
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = this@LoginViewModel.email.trim()
                    this.password = this@LoginViewModel.password
                }

                // Retrieves the verified user credentials from the current auth session
                val currentAuthUser = SupabaseClient.client.auth.currentUserOrNull()

                if (currentAuthUser != null) {
                    val authId = currentAuthUser.id
                    UserSession.setUserId(authId)

                    // Fetches user profile data and designated role from the public users table
                    val profiles = SupabaseClient.client
                        .from("users")
                        .select {
                            filter {
                                eq("id", authId)
                            }
                        }
                        .decodeList<UserDto>()

                    val userProfile = profiles.firstOrNull()
                    val isBusiness = userProfile?.role.equals("BUSINESS", ignoreCase = true)
                            || selectedRole == "Business"

                    onSuccess(isBusiness)
                } else {
                    errorMessage = "Authentication failed. Please check your credentials."
                }
            } catch (e: Exception) {
                val raw = e.localizedMessage.orEmpty()
                errorMessage = when {
                    raw.contains("Invalid login credentials", ignoreCase = true) ->
                        "Invalid email or password"
                    raw.contains("Email not confirmed", ignoreCase = true) ->
                        "Please verify your email before logging in"
                    else -> "Login failed: ${raw.ifBlank { "Unknown error" }}"
                }
            } finally {
                isLoading = false
            }
        }
    }
}