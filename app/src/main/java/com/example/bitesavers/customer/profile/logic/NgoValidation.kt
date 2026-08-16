package com.example.bitesavers.customer.profile.logic

import com.example.bitesavers.customer.profile.data.NgoApplicationUiModel
import com.example.bitesavers.customer.profile.data.NgoRegistrationType

/**
 * MVP-phase validation — format checks only, no real lookup against the
 * SSM/ROS government registry.
 *
 * Every text field is trimmed before checking, so accidental leading/
 * trailing whitespace never fails validation on its own.
 *
 * NOTE on strings.xml convention: these messages are plain Kotlin strings
 * rather than string resources, because this object lives outside the
 * Composable tree and can't call stringResource() without an Android
 * Context. Flag it in your report if strict compliance is required.
 */
object NgoValidation {

    // Org/business names legitimately contain digits (e.g. "Kechara 613 Kitchen").
    private val ORG_NAME_REGEX = Regex("^[A-Za-z0-9 .,'&()\\-]+$")
    // A person's name, on the other hand, shouldn't contain digits.
    private val PERSON_NAME_REGEX = Regex("^[A-Za-z .,'&()\\-]+$")
    private val PHONE_REGEX = Regex("^[0-9+\\-\\s]{7,15}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    private val SSM_NEW_REGEX = Regex("^\\d{12}$")
    private val SSM_OLD_REGEX = Regex("^\\d{6,7}-[A-Za-z]$")
    private val ROS_REGEX = Regex("^PPM-\\d{3}-\\d{2}-\\d{8}$")

    fun validate(application: NgoApplicationUiModel, mode: NgoFormMode): NgoFormErrors {
        val orgName = application.organizationName.trim()
        val regNumber = application.registrationNumber.trim()
        val contactName = application.contactPersonName.trim()
        val email = application.contactEmail.trim()
        val phone = application.contactPhone.trim()
        val address = application.address.trim()
        val reason = application.reasonForChange.trim()

        return NgoFormErrors(
            organizationName = when {
                orgName.isBlank() -> "Organization name is required"
                !ORG_NAME_REGEX.matches(orgName) -> "Organization name contains invalid characters"
                else -> null
            },
            registrationNumber = validateRegistrationNumber(regNumber, application.registrationType),
            contactPersonName = when {
                contactName.isBlank() -> "Contact person name is required"
                !PERSON_NAME_REGEX.matches(contactName) -> "Name cannot contain numbers"
                else -> null
            },
            contactEmail = when {
                email.isBlank() -> "Contact email is required"
                !EMAIL_REGEX.matches(email) -> "Enter a valid email address"
                else -> null
            },
            contactPhone = when {
                phone.isBlank() -> "Contact phone number is required"
                !PHONE_REGEX.matches(phone) -> "Phone number cannot contain letters"
                else -> null
            },
            causeCategory = if (application.causeCategory == null) "Please select a category" else null,
            address = if (address.isBlank()) "Address is required" else null,
            // Only required in EDIT mode — a fresh registration has nothing to explain.
            reasonForChange = if (mode == NgoFormMode.EDIT && reason.isBlank())
                "Please state a reason for this change"
            else null
        )
    }

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
    val causeCategory: String? = null,
    val address: String? = null,
    val reasonForChange: String? = null
) {
    val hasErrors: Boolean
        get() = listOfNotNull(
            organizationName, registrationNumber, contactPersonName,
            contactEmail, contactPhone, causeCategory, address, reasonForChange
        ).isNotEmpty()
}
