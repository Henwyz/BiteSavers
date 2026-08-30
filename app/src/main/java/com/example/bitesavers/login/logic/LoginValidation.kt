package com.example.bitesavers.login.logic

data class LoginValidationResult(
    val emailError: String? = null,
    val passwordError: String? = null,
    val hasErrors: Boolean = false
)

object LoginValidation {
    fun validate(email: String, pass: String): LoginValidationResult {
        var emailError: String? = null
        var passwordError: String? = null

        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank()) {
            emailError = "Email cannot be empty"
        } else if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            emailError = "Please enter a valid email address"
        }

        if (pass.isBlank()) {
            passwordError = "Password cannot be empty"
        }

        val hasErrors = emailError != null || passwordError != null
        return LoginValidationResult(emailError, passwordError, hasErrors)
    }
}