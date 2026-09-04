package com.example.bitesavers.data.repository

import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.MerchantPayoutDto
import com.example.bitesavers.data.remote.dto.StoreDto
import com.example.bitesavers.data.remote.dto.UserDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

// Repository handling store wallet balance queries, credits, payouts, and withdrawals
class MerchantWalletRepository {

    // Fetches user wallet balance directly from the users table
    suspend fun getUserWalletBalance(userId: String): Double = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank()) return@withContext 0.0
            val user = SupabaseClient.client.from("users")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<UserDto>()

            user?.walletBalance ?: 0.0
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    // Fetches the store record from Supabase
    suspend fun fetchStoreWallet(storeId: String): StoreDto? = withContext(Dispatchers.IO) {
        try {
            if (storeId.isBlank()) return@withContext null
            val response = SupabaseClient.client.from("stores")
                .select {
                    filter { eq("id", storeId) }
                }
                .decodeList<StoreDto>()

            response.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Credits order earnings to users.wallet_balance for the store owner
    suspend fun creditOrderEarnings(storeId: String, orderAmount: Double): Boolean = withContext(Dispatchers.IO) {
        try {
            val store = fetchStoreWallet(storeId) ?: return@withContext false
            val ownerId = store.ownerId ?: return@withContext false
            val currentBalance = getUserWalletBalance(ownerId)
            val newBalance = currentBalance + orderAmount

            SupabaseClient.client.from("users")
                .update({
                    set("wallet_balance", newBalance)
                }) {
                    filter { eq("id", ownerId) }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Verifies wallet balance, deducts from users.wallet_balance, and logs payout to merchant_payouts
    suspend fun requestWithdrawal(storeId: String, amount: Double, cardNumber: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val store = fetchStoreWallet(storeId) ?: return@withContext Result.failure(Exception("Store not found"))
            val ownerId = store.ownerId ?: return@withContext Result.failure(Exception("Store owner not found"))

            val currentBalance = getUserWalletBalance(ownerId)
            if (currentBalance < amount) {
                return@withContext Result.failure(Exception("Withdrawal amount exceeds available balance."))
            }

            val newBalance = currentBalance - amount

            // Deduct balance from user
            SupabaseClient.client.from("users")
                .update({
                    set("wallet_balance", newBalance)
                }) {
                    filter { eq("id", ownerId) }
                }

            // Record entry in merchant_payouts
            val payoutId = "payout_${UUID.randomUUID().toString().take(6)}"
            val payoutEntry = MerchantPayoutDto(
                id = payoutId,
                storeId = storeId,
                amount = amount,
                cardNumber = cardNumber,
                status = "PENDING"
            )

            SupabaseClient.client.from("merchant_payouts")
                .insert(payoutEntry)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Fetches payout history records for the given store
    suspend fun fetchPayoutHistory(storeId: String): List<MerchantPayoutDto> = withContext(Dispatchers.IO) {
        try {
            if (storeId.isBlank()) return@withContext emptyList()
            SupabaseClient.client.from("merchant_payouts")
                .select {
                    filter { eq("store_id", storeId) }
                }
                .decodeList<MerchantPayoutDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}