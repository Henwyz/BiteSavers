package com.example.bitesavers.LogIn.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.UserDto
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
                // Query Supabase users table matching the entered email
                val users = SupabaseClient.client
                    .from("users")
                    .select {
                        filter {
                            eq("email", email.trim())
                        }
                    }
                    .decodeList<UserDto>()

                if (users.isNotEmpty()) {
                    val user = users.first()
                    // Update global session with the real user ID from Supabase
                    UserSession.setUserId(user.id)

                    val isBusiness = user.role.equals("BUSINESS", ignoreCase = true) || selectedRole == "Business"
                    onSuccess(isBusiness)
                } else {
                    errorMessage = "No account found with this email"
                }
            } catch (e: Exception) {
                errorMessage = "Login failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isLoading = false
            }
        }
    }
}