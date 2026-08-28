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
    var passwordError by mutableStateOf(false)
        private set

    fun updateIsBusiness(value: Boolean) { isBusiness = value }
    fun updateFullName(value: String) { fullName = value }
    fun updateEmail(value: String) { email = value }
    fun updatePhoneNumber(value: String) { phoneNumber = value }
    fun updatePassword(value: String) { password = value }
    fun updateConfirmPassword(value: String) {
        confirmPassword = value
        passwordError = false
    }
    fun updateTermsAccepted(value: Boolean) { termsAccepted = value }

    fun validateAndRegister(onSuccess: () -> Unit) {
        if (password != confirmPassword) {
            passwordError = true
        } else {
            passwordError = false
            viewModelScope.launch {
                //Count how many users are already in the table
                val existingUsers = SupabaseClient.client
                    .from("users")
                    .select()
                    .decodeList<UserDto>() // fetches all users from the database into a list

                // Make the new ID like if there are 3 users, this makes "U004"
                val nextNumber = existingUsers.size + 1
                val newUserId = String.format("U%03d", nextNumber) // formats the number into U001

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

                // Insert into Supabase users table
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