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

    // Error states
    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set

    fun updateEmail(value: String) {
        email = value
        emailError = null
    }

    fun updatePassword(value: String) {
        password = value
        passwordError = null
    }

    fun updateSelectedRole(value: String) { selectedRole = value }
    fun toggleDropdown(expanded: Boolean) { isDropdownExpanded = expanded }
    fun togglePasswordVisibility() { passwordVisible = !passwordVisible }

    private fun normalizeRole(role: String?): String {
        return when (role?.trim()?.lowercase()) {
            "customer", "consumer" -> "consumer"
            "business", "merchant", "store" -> "business"
            else -> role?.trim()?.lowercase().orEmpty()
        }
    }

    fun login(onSuccess: (isBusiness: Boolean) -> Unit) {
        emailError = null
        passwordError = null

        val inputEmail = email.trim()
        val inputPassword = password

        val validationResult = LoginValidation.validate(inputEmail, inputPassword)
        if (validationResult.hasErrors) {
            emailError = validationResult.emailError
            passwordError = validationResult.passwordError
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                // Sign in with Supabase Auth
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = inputEmail
                    this.password = inputPassword
                }

                val currentAuthUser = SupabaseClient.client.auth.currentUserOrNull()

                if (currentAuthUser != null) {
                    val authId = currentAuthUser.id

                    // Fetch user details from public.users table using the exact authId
                    val profiles = SupabaseClient.client
                        .from("users")
                        .select {
                            filter {
                                eq("id", authId)
                            }
                        }
                        .decodeList<UserDto>()

                    val userProfile = profiles.firstOrNull()
                    val dbRole = normalizeRole(userProfile?.role)
                    val chosenRole = normalizeRole(selectedRole)

                    if (dbRole != chosenRole) {
                        SupabaseClient.client.auth.signOut()
                        // Clears any partial session from memory and disk
                        UserSession.clear()
                        emailError = "This account is registered as a ${userProfile?.role ?: "consumer"}, not a $selectedRole."
                    } else {
                        // Persists the authenticated user credentials and role permanently to device storage
                        UserSession.saveSession(userId = authId, role = userProfile?.role ?: "CONSUMER")
                        val isBusiness = dbRole == "business"
                        onSuccess(isBusiness)
                    }
                } else {
                    emailError = "Authentication failed. User session not found."
                }
            } catch (e: Exception) {
                val raw = e.localizedMessage.orEmpty()
                android.util.Log.e("BiteSaversLogin", "Login error: ", e)
                when {
                    raw.contains("Invalid login credentials", ignoreCase = true) -> {
                        emailError = "Email might not be registered or password is wrong"
                        passwordError = "Check your password"
                    }
                    raw.contains("Email not confirmed", ignoreCase = true) -> {
                        emailError = "Please verify your email first"
                    }
                    else -> {
                        emailError = "Login failed: ${raw.ifBlank { "Unknown error" }}"
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }
}