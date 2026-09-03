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

class SignUpViewModel : ViewModel() {
    var isBusiness by mutableStateOf(true)
        private set
    var fullName by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var termsAccepted by mutableStateOf(false)
        private set

    var fullNameError by mutableStateOf<String?>(null)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set
    var confirmPasswordError by mutableStateOf<String?>(null)
        private set

    var generalError by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set

    fun updateIsBusiness(value: Boolean) { isBusiness = value }
    fun updateFullName(value: String) {
        fullName = value
        fullNameError = null
    }
    fun updateEmail(value: String) {
        email = value
        emailError = null
    }
    fun updatePassword(value: String) {
        password = value
        passwordError = null
    }
    fun updateConfirmPassword(value: String) {
        confirmPassword = value
        confirmPasswordError = null
    }
    fun updateTermsAccepted(value: Boolean) { termsAccepted = value }

    fun validateAndRegister(onSuccess: () -> Unit) {
        val errors = SignUpValidation.validate(
            fullName = fullName,
            email = email,
            pass = password,
            confirmPass = confirmPassword,
            termsAccepted = termsAccepted
        )

        fullNameError = errors.fullName
        emailError = errors.email
        passwordError = errors.password
        confirmPasswordError = errors.confirmPassword

        if (!errors.hasErrors) {
            viewModelScope.launch {
                isLoading = true
                generalError = null
                try {
                    // Registers the account with Supabase Auth to securely hash the password
                    val authResult = SupabaseClient.client.auth.signUpWith(Email) {
                        this.email = this@SignUpViewModel.email.trim()
                        this.password = this@SignUpViewModel.password
                    }

                    // Obtains the generated UUID from Auth or the active session
                    val registeredUserId = authResult?.id
                        ?: SupabaseClient.client.auth.currentUserOrNull()?.id

                    if (registeredUserId != null) {
                        val userRole = if (isBusiness) "BUSINESS" else "CONSUMER"

                        // Matches the accompanying public user profile to the newly registered auth user ID
                        val newUser = UserDto(
                            id = registeredUserId,
                            name = fullName.trim(),
                            email = email.trim(),
                            role = userRole,
                            walletBalance = 0.0,
                            ngoStatus = "NONE",
                            ngoOrgName = null,
                        )

                        // Inserts into Supabase users table
                        SupabaseClient.client
                            .from("users")
                            .insert(newUser)

                        // Set active global user session
                        UserSession.setUserId(registeredUserId)

                        onSuccess()
                    } else {
                        generalError = "Registration succeeded. Please confirm your email before signing in."
                    }
                } catch (e: Exception) {
                    generalError = e.localizedMessage ?: "Registration failed. Please try again."
                } finally {
                    isLoading = false
                }
            }
        }
    }
}