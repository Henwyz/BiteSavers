package com.example.bitesavers.customer.discovery.data

import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole

// View mode toggle for the discovery screen
enum class DiscoveryViewMode {
    OFFERS,
    STORES
}

// Holds store item information for the 'View by Stores' list mode
data class DiscoveryStoreUiModel(
    val id: String,
    val name: String,
    val address: String,
    val rating: Double? = 4.8,
    val imageUrl: String? = null,
    val operatingHours: String = "",
    val activeOffersCount: Int = 0,
    val distanceKm: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class UserUiModel(
    val greeting: String = "Hello,",
    val displayName: String = "Guest",
    val avatarInitials: String = "G"
)

data class NearbyDealMarkerUiModel(
    val storeId: String,
    val storeName: String,
    val labelText: String,
    val latitude: Double,
    val longitude: Double,
    val offers: List<OfferUiModel> = emptyList()
)

data class NotificationUiModel(
    val id: String,
    val orderId: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false
)

data class DiscoveryUiState(
    val user: UserUiModel = UserUiModel(),
    val searchQuery: String = "",
    val notifications: List<NotificationUiModel> = emptyList(),
    val selectedCategory: DiscoveryCategory = DiscoveryCategory.ALL,
    val availableCategories: List<DiscoveryCategory> = listOf(
        DiscoveryCategory.ALL,
        DiscoveryCategory.HOT_MEALS,
        DiscoveryCategory.BAKERY,
        DiscoveryCategory.DESSERTS,
        DiscoveryCategory.BEVERAGES
    ),
    val userRole: UserRole = UserRole.CONSUMER,
    val isNgoApproved: Boolean = false, // Governs NGO privileges independent of userRole
    val viewMode: DiscoveryViewMode = DiscoveryViewMode.OFFERS,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val nearbyMarkers: List<NearbyDealMarkerUiModel> = emptyList(),
    val offers: List<OfferUiModel> = emptyList(),
    val stores: List<DiscoveryStoreUiModel> = emptyList(),
    val selectedMapOfferId: String? = null,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null
)