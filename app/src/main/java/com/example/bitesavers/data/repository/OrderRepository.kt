package com.example.bitesavers.data.repository

import android.util.Log
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.OrderDto
import com.example.bitesavers.data.remote.dto.UserDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository {
    private val client = SupabaseClient.client

    suspend fun placeOrder(
        offerId: String,
        userRole: String,
        quantity: Int,
        totalPrice: Double,
        paymentMethod: String = "BITESAVER_PAY"
    ): String? = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            val isBiteSaverPay = paymentMethod.contains("BITESAVER", ignoreCase = true) ||
                    paymentMethod.contains("PAY", ignoreCase = true)

            // 1. Guard check: Verify balance first before inserting order or updating inventory
            if (isBiteSaverPay) {
                val user = client.from("users")
                    .select { filter { eq("id", uid) } }
                    .decodeSingle<UserDto>()

                if (user.walletBalance < totalPrice) {
                    Log.e("OrderRepository", "Insufficient balance: ${user.walletBalance} < $totalPrice")
                    return@withContext null
                }
            }

            // 2. Fetch offer details
            val offer = client.from("offers")
                .select { filter { eq("id", offerId) } }
                .decodeSingle<OfferDto>()

            val order = OrderDto(
                userId = uid,
                storeId = offer.storeId ?: "s1",
                offerId = offerId,
                quantity = quantity,
                totalPrice = totalPrice,
                totalWeightKg = offer.weightKg * quantity,
                isNgoFreeClaim = userRole == "NGO" || totalPrice == 0.0,
                paymentMethod = paymentMethod,
                status = "READY_FOR_PICKUP"
            )

            // 3. Create the order
            val insertedOrder = client.from("orders")
                .insert(order) { select() }
                .decodeSingle<OrderDto>()

            // 4. Decrement offer inventory
            val newQuantity = (offer.quantityAvailable - quantity).coerceAtLeast(0)
            client.from("offers")
                .update({ set("quantity_available", newQuantity) }) {
                    filter { eq("id", offerId) }
                }

            // 5. Deduct wallet balance
            if (isBiteSaverPay) {
                val user = client.from("users")
                    .select { filter { eq("id", uid) } }
                    .decodeSingle<UserDto>()

                val newBalance = user.walletBalance - totalPrice
                client.from("users")
                    .update({ set("wallet_balance", newBalance) }) {
                        filter { eq("id", uid) }
                    }
            }

            insertedOrder.id
        } catch (e: Exception) {
            Log.e("OrderRepository", "placeOrder error: ${e.message}", e)
            null
        }
    }

    suspend fun fetchOrderById(orderId: String): OrderDto? = withContext(Dispatchers.IO) {
        try {
            client.from("orders")
                .select { filter { eq("id", orderId) } }
                .decodeSingle<OrderDto>()
        } catch (e: Exception) {
            Log.e("OrderRepository", "fetchOrderById error for ID $orderId: ${e.message}", e)
            null
        }
    }

    // In OrderRepository.kt
    suspend fun fetchCustomerOrders(): List<OrderDto> = withContext(Dispatchers.IO) {
        val currentUserId = UserSession.getUserId()
        try {
            client.from("orders")
                .select {
                    filter {
                        eq("user_id", currentUserId) // 👈 Strictly fetches orders for u1
                    }
                }
                .decodeList<OrderDto>()
        } catch (e: Exception) {
            Log.e("OrderRepository", "fetchCustomerOrders error: ${e.message}", e)
            emptyList()
        }
    }
}