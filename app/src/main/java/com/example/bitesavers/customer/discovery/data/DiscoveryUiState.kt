package com.example.bitesavers.customer.discovery.data

import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole

data class UserUiModel(
    val greeting: String = "Hello,",
    val displayName: String = "Guest",
    val avatarInitials: String = "G"
)

data class NearbyDealMarkerUiModel(
    val id: String,
    val labelPrice: String,
    val latitude: Double,
    val longitude: Double
)

data class DiscoveryUiState(
    val user: UserUiModel = UserUiModel(),
    val searchQuery: String = "",
    val selectedCategory: DiscoveryCategory = DiscoveryCategory.ALL, // <-- Change here: removed '?' and set default to ALL
    val availableCategories: List<DiscoveryCategory> = listOf(
        DiscoveryCategory.ALL,
        DiscoveryCategory.HOT_MEALS,
        DiscoveryCategory.BAKERY,
        DiscoveryCategory.DESSERTS,
        DiscoveryCategory.BEVERAGES
    ),
    val userRole: UserRole = UserRole.CONSUMER,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val nearbyMarkers: List<NearbyDealMarkerUiModel> = emptyList(),
    val offers: List<OfferUiModel> = emptyList(),
    val selectedMapOfferId: String? = null,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null
)