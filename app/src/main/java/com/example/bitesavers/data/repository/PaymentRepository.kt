package com.example.bitesavers.data.repository

import android.util.Log
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.PaymentMethodDto
import com.example.bitesavers.data.remote.dto.UserDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository {
    private val client = SupabaseClient.client

    // Generates clean, human-readable ID for payment methods matching the Supabase text ID schema
    private fun generatePaymentMethodId(): String = "pm_${System.currentTimeMillis().toString().takeLast(6)}"

    // 1. Fetch current wallet balance for the actively logged in user
    suspend fun fetchWalletBalance(): Double = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            val user = client.from("users")
                .select { filter { eq("id", uid) } }
                .decodeSingle<UserDto>()
            user.walletBalance ?: 0.0
        } catch (e: Exception) {
            Log.e("PaymentRepository", "fetchWalletBalance error for user $uid: ${e.message}", e)
            0.0
        }
    }

    // 2. Perform Top-Up (increments wallet_balance for the active user in public.users)
    suspend fun topUpWallet(amount: Double): Boolean = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            val currentBalance = fetchWalletBalance()
            val newBalance = currentBalance + amount

            client.from("users")
                .update({ set("wallet_balance", newBalance) }) {
                    filter { eq("id", uid) }
                }
            true
        } catch (e: Exception) {
            Log.e("PaymentRepository", "topUpWallet error for user $uid: ${e.message}", e)
            false
        }
    }

    // 3. Fetch all saved payment methods for the active user
    suspend fun fetchPaymentMethods(): List<PaymentMethodDto> = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            client.from("user_payment_methods")
                .select { filter { eq("user_id", uid) } }
                .decodeList<PaymentMethodDto>()
        } catch (e: Exception) {
            Log.e("PaymentRepository", "fetchPaymentMethods error for user $uid: ${e.message}", e)
            emptyList()
        }
    }

    // 4. Save a new Bank Card for the active user with a standardized pm_ prefix ID
    suspend fun saveBankCard(
        cardHolder: String,
        lastFourDigits: String,
        expiryDate: String,
        isDefault: Boolean
    ): PaymentMethodDto? = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            if (isDefault) {
                // Clear any previous default card
                client.from("user_payment_methods")
                    .update({ set("is_default", false) }) {
                        filter { eq("user_id", uid) }
                    }
            }

            val card = PaymentMethodDto(
                id = generatePaymentMethodId(),
                userId = uid,
                type = "BANK_CARD",
                cardHolder = cardHolder,
                lastFourDigits = lastFourDigits,
                expiryDate = expiryDate,
                isDefault = isDefault
            )

            client.from("user_payment_methods")
                .insert(card) { select() }
                .decodeSingle<PaymentMethodDto>()
        } catch (e: Exception) {
            Log.e("PaymentRepository", "saveBankCard error: ${e.message}", e)
            null
        }
    }

    // 5. Delete a card
    suspend fun deletePaymentMethod(cardId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.from("user_payment_methods")
                .delete { filter { eq("id", cardId) } }
            true
        } catch (e: Exception) {
            Log.e("PaymentRepository", "deletePaymentMethod error: ${e.message}", e)
            false
        }
    }

    // 6. Set a card as default
    suspend fun setDefaultCard(cardId: String): Boolean = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            client.from("user_payment_methods")
                .update({ set("is_default", false) }) {
                    filter { eq("user_id", uid) }
                }
            client.from("user_payment_methods")
                .update({ set("is_default", true) }) {
                    filter { eq("id", cardId) }
                }
            true
        } catch (e: Exception) {
            Log.e("PaymentRepository", "setDefaultCard error: ${e.message}", e)
            false
        }
    }

    // 7. Link Touch 'n Go / eWallet for active user with a standardized pm_ prefix ID
    suspend fun linkEWallet(phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            val newMethod = PaymentMethodDto(
                id = generatePaymentMethodId(),
                userId = uid,
                type = "TNG_EWALLET",
                linkedPhone = phoneNumber,
                isDefault = false
            )

            client.from("user_payment_methods").insert(newMethod)
            true
        } catch (e: Exception) {
            Log.e("PaymentRepository", "linkEWallet error: ${e.message}", e)
            false
        }
    }

    // 8. Unlink eWallet for active user
    suspend fun unlinkEWallet(): Boolean = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            client.from("user_payment_methods")
                .delete {
                    filter {
                        eq("user_id", uid)
                        eq("type", "TNG_EWALLET")
                    }
                }
            true
        } catch (e: Exception) {
            Log.e("PaymentRepository", "unlinkEWallet error: ${e.message}", e)
            false
        }
    }
}