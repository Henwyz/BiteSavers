package com.example.bitesavers.data.repository

import com.example.bitesavers.data.remote.dto.MerchantPayoutDto
import com.example.bitesavers.data.remote.dto.StoreDto
import com.example.bitesavers.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

// Repository handling store wallet balance queries, credits, payouts, and withdrawals
class MerchantWalletRepository {

    // Fetches the latest live store balance and details from Supabase
    suspend fun fetchStoreWallet(storeId: String): StoreDto? = withContext(Dispatchers.IO) {
        try {
            if (storeId.isBlank()) return@withContext null
            SupabaseClient.client.from("stores")
                .select {
                    filter { eq("id", storeId) }
                }
                .decodeSingleOrNull<StoreDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Credits order earnings to stores.balance when an order status becomes completed
    suspend fun creditOrderEarnings(storeId: String, orderAmount: Double): Boolean = withContext(Dispatchers.IO) {
        try {
            val store = fetchStoreWallet(storeId) ?: return@withContext false
            val newBalance = store.balance + orderAmount

            SupabaseClient.client.from("stores")
                .update({
                    set("balance", newBalance)
                }) {
                    filter { eq("id", storeId) }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Verifies balance >= amount, deducts balance, and inserts a pending record into merchant_payouts
    suspend fun requestWithdrawal(storeId: String, amount: Double, cardNumber: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val store = fetchStoreWallet(storeId) ?: return@withContext Result.failure(Exception("Store not found"))

            if (store.balance < amount) {
                return@withContext Result.failure(Exception("Withdrawal amount exceeds available balance."))
            }

            val newBalance = store.balance - amount

            // Deduct balance from store
            SupabaseClient.client.from("stores")
                .update({
                    set("balance", newBalance)
                }) {
                    filter { eq("id", storeId) }
                }

            // Generate unique payout ID and record entry
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