package com.example.bitesavers.business.dashboard.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfferDetails(
    @SerialName("title")
    val name: String = "",

    @SerialName("category")
    val category: String = "Bakery",

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("pickup_start")
    val pickupStart: String = "05:00 PM",

    @SerialName("pickup_end")
    val pickupEnd: String = "07:00 PM"
)

@Serializable
data class CheckOrderData(
    @SerialName("id")
    val id: String = "",

    @SerialName("user_id")
    val userId: String = "",

    @SerialName("store_id")
    val storeId: String = "",

    @SerialName("offer_id")
    val offerId: String = "",

    @SerialName("quantity")
    val quantity: Int = 1,

    @SerialName("total_price")
    val totalPrice: Double = 0.0,

    @SerialName("total_weight_kg")
    val totalWeightKg: Double = 0.0,

    @SerialName("is_ngo_free_claim")
    val isNgoFreeClaim: Boolean = false,

    @SerialName("payment_method")
    val paymentMethod: String = "BITESAVER_PAY",

    @SerialName("status")
    val status: String = "READY_FOR_PICKUP",

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("remark")
    val remark: String? = null,

    @SerialName("pickup_pin")
    val pickupPin: String? = null,

    @SerialName("cancel_reason")
    val cancelReason: String? = null,

    // Relational join mapping with offers table
    @SerialName("offers")
    val offerDetails: OfferDetails? = null
) {
    //Resolves display customer identifier from UUID (e.g. "Customer #A291").
    val formattedCustomerName: String
        get() = if (userId.length >= 4) userId.takeLast(4).uppercase() else "A291"

    // Formats order id with fixed 6 digits like 'ord_1001' -> '#BS-ORD_001001', 'ord_032090' -> '#BS-ORD_032090')
    val shortOrderId: String
        get() {
            val rawSuffix = when {
                id.startsWith("ord_", ignoreCase = true) -> id.substringAfter("ord_")
                id.contains("-") -> id.substringAfterLast("-")
                else -> id
            }
            // Extract numeric or trailing characters and pad start with '0' to ensure strictly 6 digits
            val paddedSuffix = rawSuffix.takeLast(6).padStart(6, '0')
            return "#BS-ORD_$paddedSuffix"
        }

    val displayItemName: String
        get() = offerDetails?.name ?: ""

    val displayImageUrl: String?
        get() = offerDetails?.imageUrl
}