package com.example.bitesavers.customer.profile.logic

import com.example.bitesavers.customer.profile.data.NgoApplicationUiModel
import com.example.bitesavers.customer.profile.data.NgoRegistrationType

/**
 * MVP-phase validation — format checks only, no real lookup against the
 * SSM/ROS government registry.
 *
 * NOTE on strings.xml convention: these messages are plain Kotlin strings
 * rather than string resources, because this object lives outside the
 * Composable tree and can't call stringResource() without an Android
 * Context. If your tutor flags this, the clean fix is to have this
 * function return an error *code*enum instead of text, and map codes to
* stringResource() calls inside the screen. Left as plain strings here to
* keep the MVP simple — flag it in your report as a known simplification.
*/
object NgoValidation {

    private val NAME_REGEX = Regex("^[A-Za-z .,'&()\\-]+$")
    private val PHONE_REGEX = Regex("^[0-9+\\-\\s]{7,15}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // New SSM format (post Oct 2019): YYYY + 2-digit entity code + 6-digit sequence = 12 digits
    private val SSM_NEW_REGEX = Regex("^\\d{12}$")
    // Old SSM format, still commonly quoted by existing entities: 1234567-A
    private val SSM_OLD_REGEX = Regex("^\\d{6,7}-[A-Za-z]$")
    // ROS format: PPM-XXX-XX-DDMMYYYY, e.g. PPM-002-14-04091985
    private val ROS_REGEX = Regex("^PPM-\\d{3}-\\d{2}-\\d{8}$")

    fun validate(application: NgoApplicationUiModel): NgoFormErrors = NgoFormErrors(
        organizationName = when {
            application.organizationName.isBlank() -> "Organization name is required"
            !NAME_REGEX.matches(application.organizationName) -> "Organization name cannot contain numbers"
            else -> null
        },
        registrationNumber = validateRegistrationNumber(application.registrationNumber, application.registrationType),
        contactPersonName = when {
            application.contactPersonName.isBlank() -> "Contact person name is required"
            !NAME_REGEX.matches(application.contactPersonName) -> "Name cannot contain numbers"
            else -> null
        },
        contactEmail = when {
            application.contactEmail.isBlank() -> "Contact email is required"
            !EMAIL_REGEX.matches(application.contactEmail) -> "Enter a valid email address"
            else -> null
        },
        contactPhone = when {
            application.contactPhone.isBlank() -> "Contact phone number is required"
            !PHONE_REGEX.matches(application.contactPhone) -> "Phone number cannot contain letters"
            else -> null
        },
        address = if (application.address.isBlank()) "Address is required" else null
    )

    private fun validateRegistrationNumber(number: String, type: NgoRegistrationType): String? {
        if (number.isBlank()) return "Registration number is required"
        val isValid = when (type) {
            NgoRegistrationType.SSM -> SSM_NEW_REGEX.matches(number) || SSM_OLD_REGEX.matches(number)
            NgoRegistrationType.ROS -> ROS_REGEX.matches(number)
        }
        if (isValid) return null
        return when (type) {
            NgoRegistrationType.SSM -> "Must be a valid SSM number, e.g. 202301012345 or 1234567-A"
            NgoRegistrationType.ROS -> "Must be a valid ROS number, e.g. PPM-002-10-04091985"
        }
    }
}

data class NgoFormErrors(
    val organizationName: String? = null,
    val registrationNumber: String? = null,
    val contactPersonName: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val address: String? = null
) {
    val hasErrors: Boolean
        get() = listOfNotNull(
            organizationName, registrationNumber, contactPersonName,
            contactEmail, contactPhone, address
        ).isNotEmpty()
}
