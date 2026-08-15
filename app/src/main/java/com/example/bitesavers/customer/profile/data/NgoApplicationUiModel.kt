package com.example.bitesavers.customer.profile.data

//Jyan Lim
import android.net.Uri

/**
 * MVP scope: NgoValidation (in the logic package) only checks FORMAT —
 * there's no real check against the SSM/ROS government registry, and
 * certificateUri/certificateFileName are just captured for display, not
 * actually uploaded anywhere in this phase.
 */
data class NgoApplicationUiModel(
    val organizationName: String = "",
    val registrationType: NgoRegistrationType = NgoRegistrationType.SSM,
    val registrationNumber: String = "",
    val contactPersonName: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val causeCategory: NgoCauseCategory = NgoCauseCategory.FOOD_BANK,
    val address: String = "",
    val agreedToTerms: Boolean = false,
    val certificateUri: Uri? = null,
    val certificateFileName: String? = null
){
    val isValid: Boolean
        get() = organizationName.isNotBlank() &&
                registrationNumber.isNotBlank() &&
                contactPersonName.isNotBlank() &&
                contactEmail.isNotBlank() &&
                contactPhone.isNotBlank() &&
                address.isNotBlank() &&
                agreedToTerms
}
