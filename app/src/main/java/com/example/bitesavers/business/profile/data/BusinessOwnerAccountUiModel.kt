package com.example.bitesavers.business.profile.data

/**
 * Just the owner's personal account fields (from the `users` table) — kept
 * separate from BusinessProfileUiModel, which represents the STORE/restaurant info.
 */
data class BusinessOwnerAccountUiModel(
    val name: String = "",
    val email: String = ""
)