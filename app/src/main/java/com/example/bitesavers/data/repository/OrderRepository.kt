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
import com.example.bitesavers.data.remote.dto.NotificationDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

// DTO to resolve merchant owner_id from stores table
@Serializable
private data class StoreOwnerInfo(
    val id: String,
    @SerialName("owner_id")
    val ownerId: String
)
class OrderRepository {
    private val client = SupabaseClient.client

    private fun generateOrderId(): String = "ord_${System.currentTimeMillis().toString().takeLast(6)}"

    suspend fun placeOrder(
        offerId: String,
        userRole: String = "CONSUMER",
        quantity: Int,
        totalPrice: Double,
        paymentMethod: String = "BITESAVER_PAY"
    ): String? = withContext(Dispatchers.IO) {
        val uid = UserSession.getUserId()
        try {
            // 1. Fetch user to verify approved NGO status and balance
            val user = client.from("users")
                .select { filter { eq("id", uid) } }
                .decodeSingle<UserDto>()

            val isNgoApproved = user.ngoStatus.equals("APPROVED", ignoreCase = true)
            val isFreeClaim = totalPrice <= 0.0 || (isNgoApproved && totalPrice == 0.0)

            val isPaidViaWallet = !isFreeClaim && (
                    paymentMethod.contains("BITESAVER", ignoreCase = true) ||
                            paymentMethod.contains("PAY", ignoreCase = true)
                    )

            // Guard check: Only verify wallet balance if money must be deducted
            if (isPaidViaWallet) {
                val currentBalance = user.walletBalance
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

            val pickupPin = Random.nextInt(1000, 10000).toString()
            val cleanOrderId = generateOrderId()

            // 3. Construct Order record with safe payment method string
            val order = OrderDto(
                id = cleanOrderId,
                userId = uid,
                storeId = offer.storeId ?: "store_01",
                offerId = offerId,
                quantity = quantity,
                totalPrice = if (isFreeClaim) 0.0 else totalPrice,
                totalWeightKg = weight * quantity,
                isNgoFreeClaim = isFreeClaim,
                paymentMethod = if (isFreeClaim) "CASH_ON_PICKUP" else if (paymentMethod.isBlank()) "BITESAVER_PAY" else paymentMethod,
                status = "READY_FOR_PICKUP",
                pickupPin = pickupPin
            )

            // 4. Create the order
            val insertedOrder = client.from("orders")
                .insert(order) { select() }
                .decodeSingle<OrderDto>()

            // 5. Decrement offer inventory
            val newQuantity = (available - quantity).coerceAtLeast(0)
            client.from("offers")
                .update({ set("quantity_available", newQuantity) }) {
                    filter { eq("id", offerId) }
                }

            // 6. Deduct wallet balance ONLY for non-free orders paid via wallet
            if (isPaidViaWallet) {
                val updatedBalance = user.walletBalance - totalPrice
                client.from("users")
                    .update({ set("wallet_balance", updatedBalance) }) {
                        filter { eq("id", uid) }
                    }
            }
// 6. Credit merchant wallet balance if paid via BiteSaver Pay
            val resolvedStoreId = offer.storeId ?: "store_01"
            var merchantOwnerId: String? = null

            try {
                val storeList = client.from("stores")
                    .select { filter { eq("id", resolvedStoreId) } }
                    .decodeList<StoreOwnerInfo>()

                merchantOwnerId = storeList.firstOrNull()?.ownerId

                if (!merchantOwnerId.isNullOrBlank() && isPaidViaWallet) {
                    val merchant = client.from("users")
                        .select { filter { eq("id", merchantOwnerId) } }
                        .decodeSingle<UserDto>()

                    val updatedMerchantBalance = (merchant.walletBalance ?: 0.0) + totalPrice
                    client.from("users")
                        .update({ set("wallet_balance", updatedMerchantBalance) }) {
                            filter { eq("id", merchantOwnerId) }
                        }
                }
            } catch (e: Exception) {
                Log.e("OrderRepository", "Failed to update merchant wallet: ${e.message}")
            }

            // 7. Insert new order notification for the store merchant
            if (!merchantOwnerId.isNullOrBlank()) {
                try {
                    val notifId = "notif_${UUID.randomUUID().toString().take(8)}"
                    val merchantNotif = NotificationDto(
                        id = notifId,
                        userId = merchantOwnerId,
                        orderId = insertedOrder.id,
                        title = "New Order Received! 🛍️",
                        message = "You received a new order (${insertedOrder.id}) for $quantity x ${offer.title}.",
                        isRead = false
                    )
                    client.from("user_notifications").insert(merchantNotif)
                } catch (e: Exception) {
                    Log.e("OrderRepository", "Failed to notify merchant: ${e.message}")
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