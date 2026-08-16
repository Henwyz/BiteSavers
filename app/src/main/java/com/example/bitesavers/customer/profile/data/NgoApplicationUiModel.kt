package com.example.bitesavers.customer.profile.data

import android.net.Uri

/**
 * MVP scope: NgoValidation (in the logic package) only checks FORMAT —
 * there's no real check against the SSM/ROS government registry, and
 * certificateUri/certificateFileName are just captured for display, not
 * actually uploaded anywhere in this phase.
 *
 * `causeCategory` is nullable and defaults to null so the dropdown starts
 * on a real "Select a Category" placeholder rather than silently
 * pre-selecting the first option — see NgoValidation for the required check.
 *
 * `reasonForChange` is only shown/required when this model is used inside
 * an EDIT-mode form — it's simply ignored (stays blank) during a fresh
 * registration.
 */
data class NgoApplicationUiModel(
    val organizationName: String = "",
    val registrationType: NgoRegistrationType = NgoRegistrationType.SSM,
    val registrationNumber: String = "",
    val contactPersonName: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val causeCategory: NgoCauseCategory? = null,
    val address: String = "",
    val agreedToTerms: Boolean = false,
    val certificateUri: Uri? = null,
    val certificateFileName: String? = null,
    val reasonForChange: String = ""
) {
    /** Trims leading/trailing whitespace on every text field before validating or saving. */
    fun trimmed(): NgoApplicationUiModel = copy(
        organizationName = organizationName.trim(),
        registrationNumber = registrationNumber.trim(),
        contactPersonName = contactPersonName.trim(),
        contactEmail = contactEmail.trim(),
        contactPhone = contactPhone.trim(),
        address = address.trim(),
        reasonForChange = reasonForChange.trim()
    )
}
