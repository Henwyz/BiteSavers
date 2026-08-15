package com.example.bitesavers.data.repository

import com.example.bitesavers.data.mapper.toUiModel
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.OfferDto
import com.example.bitesavers.data.remote.dto.OrderDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfferRepository {

    private val client = SupabaseClient.client

    /**
     * 1. Discovery Screen: Fetch all active offers with stock > 0
     */
    suspend fun fetchOffers(): List<OfferUiModel> = withContext(Dispatchers.IO) {
        try {
            val dtoList = client.from("offers")
                .select {
                    filter {
                        gt("quantity_left", 0)
                    }
                }
                .decodeList<OfferDto>()

            dtoList.map { it.toUiModel() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 2. Food Detail Screen: Fetch a single offer by its unique ID
     */
    suspend fun fetchOfferById(offerId: String): OfferUiModel? = withContext(Dispatchers.IO) {
        try {
            val dto = client.from("offers")
                .select {
                    filter {
                        eq("id", offerId)
                    }
                }
                .decodeSingle<OfferDto>()

            dto.toUiModel()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 3. Checkout Screen: Place an order and record it in Supabase
     */
    suspend fun placeOrder(
        offerId: String,
        userRole: String,
        quantity: Int,
        totalPrice: Double,
        hoursToClose: Int
    ): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Insert into orders table
            val order = OrderDto(
                offerId = offerId,
                userRole = userRole,
                quantity = quantity,
                totalPrice = totalPrice,
                status = "READY_FOR_PICKUP",
                pickupWindowClose = "Within $hoursToClose hour(s)"
            )

            val insertedOrder = client.from("orders")
                .insert(order) {
                    select()
                }
                .decodeSingle<OrderDto>()

            // 2. Fetch current stock and deduct it
            val currentOfferDto = client.from("offers")
                .select {
                    filter { eq("id", offerId) }
                }
                .decodeSingle<OfferDto>()

            val newQuantity = (currentOfferDto.quantityLeft - quantity).coerceAtLeast(0)

            client.from("offers")
                .update({
                    set("quantity_left", newQuantity)
                }) {
                    filter { eq("id", offerId) }
                }

            insertedOrder.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 4. Ticket Screen: Fetch a placed order by its order ID
     */
    suspend fun fetchOrderById(orderId: String): OrderDto? = withContext(Dispatchers.IO) {
        try {
            client.from("orders")
                .select {
                    filter {
                        eq("id", orderId)
                    }
                }
                .decodeSingle<OrderDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}