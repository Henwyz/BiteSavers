package com.example.bitesavers.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val name: String = "",
    val email: String = "",
    val role: String = "CONSUMER",
    @SerialName("wallet_balance") val walletBalance: Double = 0.0,
    @SerialName("ngo_status") val ngoStatus: String = "NONE",
    @SerialName("ngo_org_name") val ngoOrgName: String? = null,
    @SerialName("meals_rescued") val mealsRescued: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)



/**
 * Partial-update payload — only the two columns this app ever changes on
 * `users`. Using a small dedicated class (rather than the full UserDto)
 * means we never accidentally overwrite name/email/wallet_balance/etc.
 * with stale local values on an update call.
 */
@Serializable
data class UserNgoStatusUpdateDto(
    @SerialName("ngo_status") val ngoStatus: String,
    @SerialName("ngo_org_name") val ngoOrgName: String? = null
)

@Serializable
data class UserProfileUpdateDto(
    val name: String,
    val email: String
)