package com.example.bitesavers.customer.profile.data

/**
 * UI-facing model for the logged-in consumer's Profile screen.
 *
 * Check with the team before merging: this may overlap with whatever
 * user/profile model already exists near UserRole.kt. If one exists,
 * just add `ngoStatus`, `ngoOrgName`, and `mealsRescued` to it instead
 * of introducing a second model.
 */
data class UserProfileUiModel(
    val id: String,
    val name: String,
    val email: String,
    val avatarInitials: String,
    val memberSinceLabel: String, // e.g. "Member since 7 June 2026"
    val walletBalance: Double,
    val ngoStatus: NgoStatus = NgoStatus.NONE,
    val ngoOrgName: String? = null,
    val mealsRescued: Int = 0,
    val moneySaved: Double = 0.0,
    val co2ReducedKg: Double = 0.0
)

enum class NgoStatus {
    NONE,     // regular consumer
    PENDING,  // application submitted, awaiting review
    APPROVED  // equivalent to isNGO = true elsewhere in the app —
    // unlocks "Claim for Free" / category_free in Discovery
}