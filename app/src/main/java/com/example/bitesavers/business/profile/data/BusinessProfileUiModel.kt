package com.example.bitesavers.business.profile.data

enum class BusinessEditTab(val displayLabel: String) {
    ACCOUNT("Account Details"),
    BUSINESS("Business Details")
}

data class BusinessProfileUiModel(
    val id: String = "",
    val businessName: String = "Loading...",
    val verificationId: String = "",
    val isVerified: Boolean = false,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val address: String = "",
    val phone: String = "",
    val category: String = "",
    val operatingHours: String = "",
    val cleanupHours: String = "",
    val balance: Double = 0.0
)

data class BusinessAccountEditUiModel(
    val name: String = "",
    val email: String = "",
    val password: String = ""
) {
    fun trimmed() = copy(name = name.trim(), email = email.trim(), password = password.trim())
}

data class BusinessDetailsEditUiModel(
    val businessName: String = "",
    val address: String = "",
    val phone: String = "",
    val operatingHours: String = "",
    val cleanupHours: String = "",
    val reasonForChange: String = "",
    val agreedToTerms: Boolean = false
) {
    fun trimmed() = copy(
        businessName = businessName.trim(),
        address = address.trim(),
        phone = phone.trim(),
        operatingHours = operatingHours.trim(),
        cleanupHours = cleanupHours.trim(),
        reasonForChange = reasonForChange.trim()
    )
}