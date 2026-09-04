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
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class UserProfileUpdateDto(
    @SerialName("name") val name: String,
    @SerialName("email") val email: String
)

@Serializable
data class UserNgoStatusUpdateDto(
    @SerialName("ngo_status") val ngoStatus: String,
    @SerialName("ngo_org_name") val ngoOrgName: String? = null
)