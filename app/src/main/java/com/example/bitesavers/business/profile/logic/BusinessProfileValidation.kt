package com.example.bitesavers.business.profile.logic

import com.example.bitesavers.business.profile.data.BusinessAccountEditUiModel
import com.example.bitesavers.business.profile.data.BusinessDetailsEditUiModel

data class BusinessAccountFormErrors(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null
) {
    val hasErrors: Boolean get() = listOfNotNull(name, email, password).isNotEmpty()
}

data class BusinessDetailsFormErrors(
    val businessName: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val operatingHours: String? = null,
    val cleanupHours: String? = null,
    val reasonForChange: String? = null
) {
    val hasErrors: Boolean get() = listOfNotNull(
        businessName, address, phone, operatingHours, cleanupHours, reasonForChange
    ).isNotEmpty()
}

object BusinessProfileValidation {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PHONE_REGEX = Regex("^[0-9+\\-\\s]{7,15}$")

    fun validateAccount(model: BusinessAccountEditUiModel): BusinessAccountFormErrors {
        val trimmed = model.trimmed()
        return BusinessAccountFormErrors(
            name = if (trimmed.name.isBlank()) "Name is required" else null,
            email = when {
                trimmed.email.isBlank() -> "Email is required"
                !EMAIL_REGEX.matches(trimmed.email) -> "Enter a valid email address"
                else -> null
            },
            // Validates that if a new password is provided, it must be at least 8 characters long
            password = if (trimmed.password.isNotEmpty() && trimmed.password.length < 8) {
                "Password must be at least 8 characters"
            } else null
        )
    }

    fun validateBusiness(model: BusinessDetailsEditUiModel): BusinessDetailsFormErrors {
        val trimmed = model.trimmed()
        return BusinessDetailsFormErrors(
            businessName = if (trimmed.businessName.isBlank()) "Business name is required" else null,
            address = if (trimmed.address.isBlank()) "Address is required" else null,
            phone = when {
                trimmed.phone.isBlank() -> "Phone number is required"
                !PHONE_REGEX.matches(trimmed.phone) -> "Enter a valid phone number"
                else -> null
            },
            operatingHours = if (trimmed.operatingHours.isBlank()) "Operating hours are required" else null,
            cleanupHours = if (trimmed.cleanupHours.isBlank()) "Clean-up hours are required" else null,
            reasonForChange = if (trimmed.reasonForChange.isBlank()) "Please state a reason for this change" else null
        )
    }
}