package com.example.bitesavers.data.repository

import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.MerchantPayoutDto
import com.example.bitesavers.data.remote.dto.StoreDto
import com.example.bitesavers.data.remote.dto.UserDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class PayoutRepository {
    private val client = SupabaseClient.client

    // Fetch store profile information
    suspend fun getStorePayoutInfo(storeId: String): StoreDto? = withContext(Dispatchers.IO) {
        try {
            client.from("stores")
                .select {
                    filter { eq("id", storeId) }
                }
                .decodeSingle<StoreDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Fetches live user wallet balance from the unified users table
    suspend fun getUserWalletBalance(userId: String): Double = withContext(Dispatchers.IO) {
        try {
            val user = client.from("users")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingle<UserDto>()
            user.walletBalance ?: 0.0
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    // Fetch past payout history for this store
    suspend fun getPayoutHistory(storeId: String): List<MerchantPayoutDto> = withContext(Dispatchers.IO) {
        try {
            client.from("merchant_payouts")
                .select {
                    filter { eq("store_id", storeId) }
                }
                .decodeList<MerchantPayoutDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Requests a payout and deducts the amount from the user's unified wallet balance
    suspend fun requestPayout(
        userId: String,
        storeId: String,
        amount: Double,
        cardNumber: String,
        currentBalance: Double
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (amount <= 0.0 || amount > currentBalance) return@withContext false

            val payoutId = "payout_" + UUID.randomUUID().toString().take(8)

            // 1. Record payout transaction in merchant_payouts with userId reference
            val newPayout = MerchantPayoutDto(
                id = payoutId,
                userId = userId,
                storeId = storeId,
                amount = amount,
                cardNumber = cardNumber,
                status = "PENDING"
            )
            client.from("merchant_payouts").insert(newPayout)

            // 2. Deduct requested amount from users.wallet_balance
            val newBalance = currentBalance - amount
            client.from("users").update(
                mapOf("wallet_balance" to newBalance)
            ) {
                filter { eq("id", userId) }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}