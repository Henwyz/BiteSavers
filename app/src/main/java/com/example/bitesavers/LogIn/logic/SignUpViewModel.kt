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
import java.util.UUID

class SignUpViewModel : ViewModel() {
    var isBusiness by mutableStateOf(true)
        private set
    var fullName by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var phoneNumber by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var termsAccepted by mutableStateOf(false)
        private set

    // Below this 5 variable is about the error state
    var fullNameError by mutableStateOf<String?>(null)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set
    var phoneError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set
    var confirmPasswordError by mutableStateOf<String?>(null)
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
    fun updatePhoneNumber(value: String) {
        phoneNumber = value
        phoneError = null
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
        // Run validation using our SignUpValidation object
        val errors = SignUpValidation.validate(
            fullName = fullName,
            email = email,
            phone = phoneNumber,
            pass = password,
            confirmPass = confirmPassword,
            termsAccepted = termsAccepted
        )

        // Assign errors to each specific variable
        // so the UI for each textfields will turn red and show error text
        fullNameError = errors.fullName
        emailError = errors.email
        phoneError = errors.phone
        passwordError = errors.password
        confirmPasswordError = errors.confirmPassword

        if (!errors.hasErrors) {
            viewModelScope.launch {
                //Count how many users are already in the table
                val existingUsers = SupabaseClient.client
                    .from("users")
                    .select()
                    .decodeList<UserDto>() // fetches all users from the database into a list

                // Make the new ID like if there are 3 users, this makes U4
                val nextNumber = existingUsers.size + 1
                val newUserId = "U$nextNumber" // format the number such as U1 or U2

                val userRole = if (isBusiness) "BUSINESS" else "CONSUMER"

                // Create user DTO matching your teammate's schema
                val newUser = UserDto(
                    id = newUserId,
                    name = fullName.trim(),
                    email = email.trim(),
                    role = userRole,
                    walletBalance = 0.0,
                    ngoStatus = "NONE",
                    ngoOrgName = null,
                    mealsRescued = 0
                )

                // this is use for insert into Supabase users table
                SupabaseClient.client
                    .from("users")
                    .insert(newUser)

                // Set active global user session
                UserSession.setUserId(newUserId)

                onSuccess()
             }
        }
    }
}