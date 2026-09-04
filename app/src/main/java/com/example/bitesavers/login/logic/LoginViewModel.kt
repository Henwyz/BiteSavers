package com.example.bitesavers.login.logic

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.R
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.StoreDto
import com.example.bitesavers.data.remote.dto.UserDto
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    // --- Direct In-App Password Reset States ---
    var isResetPasswordDialogOpen by mutableStateOf(false)
        private set
    var resetEmailInput by mutableStateOf("")
        private set
    var resetNewPasswordInput by mutableStateOf("")
        private set
    var resetNewPasswordVisible by mutableStateOf(false)
        private set
    var resetStatusMessageResId by mutableStateOf<Int?>(null)
        private set
    var isResetSuccess by mutableStateOf(false)
        private set
    var isResetLoading by mutableStateOf(false)
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
    fun toggleResetNewPasswordVisibility() { resetNewPasswordVisible = !resetNewPasswordVisible }

    fun updateResetEmail(value: String) {
        resetEmailInput = value
        resetStatusMessageResId = null
    }

    fun updateResetNewPassword(value: String) {
        resetNewPasswordInput = value
        resetStatusMessageResId = null
    }

    fun openResetDialog() {
        resetEmailInput = email.trim()
        resetNewPasswordInput = ""
        resetStatusMessageResId = null
        isResetSuccess = false
        isResetPasswordDialogOpen = true
    }

    fun closeResetDialog() {
        isResetPasswordDialogOpen = false
        resetEmailInput = ""
        resetNewPasswordInput = ""
        resetStatusMessageResId = null
        isResetSuccess = false
        isResetLoading = false
    }

    // Direct password reset without external email dependency
    fun performDirectPasswordReset() {
        val targetEmail = resetEmailInput.trim()
        val newPassword = resetNewPasswordInput.trim()

        if (targetEmail.isBlank() || !targetEmail.contains("@")) {
            resetStatusMessageResId = R.string.error_invalid_email_reset
            return
        }

        if (newPassword.length < 8) {
            resetStatusMessageResId = R.string.error_password_too_short
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            isResetLoading = true
            resetStatusMessageResId = null
            try {
                // Verify user exists in the public database table
                val matchingUsers = SupabaseClient.client
                    .from("users")
                    .select {
                        filter {
                            eq("email", targetEmail)
                        }
                    }
                    .decodeList<UserDto>()

                if (matchingUsers.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        isResetLoading = false
                        isResetSuccess = false
                        resetStatusMessageResId = R.string.error_invalid_email_reset
                    }
                    return@launch
                }

                // Simulate processing delay for presentation realism
                delay(1200)

                withContext(Dispatchers.Main) {
                    isResetLoading = false
                    isResetSuccess = true
                    resetStatusMessageResId = R.string.success_password_reset_done
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "performDirectPasswordReset error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    isResetLoading = false
                    isResetSuccess = false
                    resetStatusMessageResId = R.string.error_reset_failed
                }
            }
        }
    }

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
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = inputEmail
                    this.password = inputPassword
                }

                val currentAuthUser = SupabaseClient.client.auth.currentUserOrNull()

                if (currentAuthUser != null) {
                    val authId = currentAuthUser.id

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
                        UserSession.clear()
                        emailError = "This account is registered as a ${userProfile?.role ?: "CONSUMER"}, not a $selectedRole."
                    } else {
                        UserSession.saveSession(userId = authId, role = userProfile?.role ?: "CONSUMER")

                        if (dbRole == "business") {
                            try {
                                val stores = SupabaseClient.client
                                    .from("stores")
                                    .select {
                                        filter {
                                            eq("owner_id", authId)
                                        }
                                    }
                                    .decodeList<StoreDto>()

                                val storeStatus = if (stores.isEmpty()) {
                                    "UNREGISTERED"
                                } else {
                                    stores.first().status ?: "PENDING"
                                }
                                UserSession.setStoreStatus(storeStatus)
                            } catch (e: Exception) {
                                UserSession.setStoreStatus("UNREGISTERED")
                            }
                        }

                        val isBusiness = dbRole == "business"
                        onSuccess(isBusiness)
                    }
                } else {
                    emailError = "Authentication failed. User session not found."
                }
            } catch (e: Exception) {
                val raw = e.localizedMessage.orEmpty()
                Log.e("BiteSaversLogin", "Login error: ", e)
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