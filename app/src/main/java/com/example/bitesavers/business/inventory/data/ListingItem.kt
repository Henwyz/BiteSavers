package com.example.bitesavers.business.inventory.data

data class ListingItem(
    val id: String,
    val name: String,
    val description: String = "",
    val category: String,
    val originalPrice: Double,
    val discountPrice: Double,
    val quantity: Int,
    val expiryTime: String,
    val status: String = "Active"
) {
    val discountPercent: Int
        get() = if (originalPrice > 0) {
            (((originalPrice - discountPrice) / originalPrice) * 100).toInt()
        } else 0
}