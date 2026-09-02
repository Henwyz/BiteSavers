package com.example.bitesavers.customer.store.data

// Holds formatted store profile information for display on the restaurant detail page
data class StoreDetailUiModel(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val rating: Double? = 4.8,
    val contactPhone: String? = null,
    val operatingHours: String = "",
    val imageUrl: String? = null
)