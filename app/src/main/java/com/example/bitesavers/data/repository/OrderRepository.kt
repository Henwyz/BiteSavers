package com.example.bitesavers.data.repository

import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.OrderDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository {
    private val client = SupabaseClient.client

    // 3. Checkout Screen: Place an order
    suspend fun placeOrder(
        offerId: String,
        userRole: String,
        quantity: Int,
        totalPrice: Double,
        hoursToClose: Int,
        paymentMethod: String = "BITESAVER_PAY" //Added with default fallback
    ): String? = withContext(Dispatchers.IO) {
        try {
            val order = OrderDto(
                offerId = offerId,
                userRole = userRole,
                quantity = quantity,
                totalPrice = totalPrice,
                paymentMethod = paymentMethod,
                status = "READY_FOR_PICKUP",
                pickupWindowClose = "Within $hoursToClose hour(s)"
            )

            val insertedOrder = client.from("orders").insert(order) { select() }.decodeSingle<OrderDto>()

            val currentOfferDto = client.from("offers").select { filter { eq("id", offerId) } }.decodeSingle<OfferDto>()
            val newQuantity = (currentOfferDto.quantityLeft - quantity).coerceAtLeast(0)

            client.from("offers").update({ set("quantity_left", newQuantity) }) { filter { eq("id", offerId) } }

            insertedOrder.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 4. Ticket Screen: Fetch a single order
    suspend fun fetchOrderById(orderId: String): OrderDto? = withContext(Dispatchers.IO) {
        try {
            client.from("orders").select { filter { eq("id", orderId) } }.decodeSingle<OrderDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchCustomerOrders(): List<OrderDto> = withContext(Dispatchers.IO) {
        try {
            client.from("orders")
                .select {
                    // Optional: filter by userRole or status here if future
                }
                .decodeList<OrderDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}