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

    // for the error state
    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set

    fun updateEmail(value: String) {
        email = value
        emailError = null // Clear error when typing
    }

    fun updatePassword(value: String) {
        password = value
        passwordError = null // Clear error when typing
    }

    fun updateSelectedRole(value: String) { selectedRole = value }
    fun toggleDropdown(expanded: Boolean) { isDropdownExpanded = expanded }
    fun togglePasswordVisibility() { passwordVisible = !passwordVisible }

    fun login(onSuccess: (isBusiness: Boolean) -> Unit) {
        // this is use for reset previous errors
        emailError = null
        passwordError = null

        val validationResult = LoginValidation.validate(email, password)
        if (validationResult.hasErrors) {
            emailError = validationResult.emailError
            passwordError = validationResult.passwordError
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = this@LoginViewModel.email.trim()
                    this.password = this@LoginViewModel.password
                }

                val currentAuthUser = SupabaseClient.client.auth.currentUserOrNull()

                if (currentAuthUser != null) {
                    val authId = currentAuthUser.id
                    UserSession.setUserId(authId)

                    val profiles = SupabaseClient.client
                        .from("users")
                        .select {
                            filter {
                                eq("id", authId)
                            }
                        }
                        .decodeList<UserDto>()

                    val userProfile = profiles.firstOrNull()
                    val dbRole = userProfile?.role?.lowercase() ?: "consumer"
                    val chosenRole = selectedRole.lowercase()

                    if (dbRole != chosenRole) {
                        SupabaseClient.client.auth.signOut()
                        UserSession.clear()
                        emailError = "This account is registered as a $dbRole, not a $selectedRole."
                    } else {
                        val isBusiness = dbRole == "business"
                        onSuccess(isBusiness)
                    }
                }else {
                    emailError = "Authentication failed."
                }
            } catch (e: Exception) {
                val raw = e.localizedMessage.orEmpty()
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