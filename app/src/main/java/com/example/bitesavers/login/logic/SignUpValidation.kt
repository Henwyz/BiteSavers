package com.example.bitesavers.login.logic

object SignUpValidation {

    private val NAME_REGEX = Regex("^[A-Za-z .,'&()\\-]+$")
    private val PHONE_REGEX = Regex("^[0-9+\\-\\s]{7,15}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validate(
        fullName: String,
        email: String,
        phone: String,
        pass: String,
        confirmPass: String,
        termsAccepted: Boolean
    ): SignUpFormErrors {
        val nameTrimmed = fullName.trim()
        val emailTrimmed = email.trim()
        val phoneTrimmed = phone.trim()

        return SignUpFormErrors(
            fullName = when {
                nameTrimmed.isBlank() -> "Full name is required"
                !NAME_REGEX.matches(nameTrimmed) -> "Name cannot contain numbers"
                else -> null
            },
            email = when {
                emailTrimmed.isBlank() -> "Email address is required"
                !EMAIL_REGEX.matches(emailTrimmed) -> "Enter a valid email address"
                else -> null
            },
            phone = when {
                phoneTrimmed.isBlank() -> "Phone number is required"
                !PHONE_REGEX.matches(phoneTrimmed) -> "Enter a valid phone number"
                else -> null
            },
            password = when {
                pass.isBlank() -> "Password is required"
                pass.length < 8 -> "Password must be at least 8 characters"
                else -> null
            },
            confirmPassword = when {
                confirmPass.isBlank() -> "Please confirm your password"
                pass != confirmPass -> "Passwords do not match"
                else -> null
            },
            terms = if (!termsAccepted) "You must accept the terms and conditions" else null
        )
    }
}

data class SignUpFormErrors(
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
    val terms: String? = null
) {
    val hasErrors: Boolean
        get() = listOfNotNull(fullName, email, phone, password, confirmPassword, terms).isNotEmpty()
}