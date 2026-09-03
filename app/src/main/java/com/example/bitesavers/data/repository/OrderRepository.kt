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
import kotlin.random.Random

class OrderRepository {
    private val client = SupabaseClient.client

    // Generates clean, human-readable IDs matching Supabase seed format (e.g. ord_1725301234)
    private fun generateOrderId(): String = "ord_${System.currentTimeMillis().toString().takeLast(6)}"

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

                val currentBalance = user.walletBalance ?: 0.0
                if (currentBalance < totalPrice) {
                    Log.e("OrderRepository", "Insufficient balance: $currentBalance < $totalPrice")
                    return@withContext null
                }
            }

            // 2. Fetch offer details
            val offer = client.from("offers")
                .select { filter { eq("id", offerId) } }
                .decodeSingle<OfferDto>()

            val weight = offer.weightKg ?: 0.3
            val available = offer.quantityAvailable ?: 0

            // Generates a 4-digit numeric pickup verification PIN
            val pickupPin = Random.nextInt(1000, 10000).toString()
            val cleanOrderId = generateOrderId()

            val order = OrderDto(
                id = cleanOrderId,
                userId = uid,
                storeId = offer.storeId ?: "store_01",
                offerId = offerId,
                quantity = quantity,
                totalPrice = totalPrice,
                totalWeightKg = weight * quantity,
                isNgoFreeClaim = userRole == "NGO" || totalPrice == 0.0,
                paymentMethod = paymentMethod,
                status = "READY_FOR_PICKUP",
                pickupPin = pickupPin
            )

            // 3. Create the order with readable ID
            val insertedOrder = client.from("orders")
                .insert(order) { select() }
                .decodeSingle<OrderDto>()

            // 4. Decrement offer inventory
            val newQuantity = (available - quantity).coerceAtLeast(0)
            client.from("offers")
                .update({ set("quantity_available", newQuantity) }) {
                    filter { eq("id", offerId) }
                }

            // 5. Deduct wallet balance if paid via BiteSaver Pay
            if (isBiteSaverPay) {
                val user = client.from("users")
                    .select { filter { eq("id", uid) } }
                    .decodeSingle<UserDto>()

                val updatedBalance = (user.walletBalance ?: 0.0) - totalPrice
                client.from("users")
                    .update({ set("wallet_balance", updatedBalance) }) {
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

    // Fetches orders strictly for the current active user session
    suspend fun fetchCustomerOrders(): List<OrderDto> = withContext(Dispatchers.IO) {
        val currentUserId = UserSession.getUserId()
        try {
            client.from("orders")
                .select {
                    filter {
                        eq("user_id", currentUserId)
                    }
                }
                .decodeList<OrderDto>()
        } catch (e: Exception) {
            Log.e("OrderRepository", "fetchCustomerOrders error: ${e.message}", e)
            emptyList()
        }
    }

    // Fetches orders for a specific user ID to populate notifications and order history
    suspend fun fetchOrdersByUserId(userId: String): List<OrderDto> = withContext(Dispatchers.IO) {
        try {
            client.from("orders")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<OrderDto>()
        } catch (e: Exception) {
            Log.e("OrderRepository", "fetchOrdersByUserId error: ${e.message}", e)
            emptyList()
        }
    }
}