package com.example.bitesavers.discovery.data

enum class UserRole {
    CONSUMER,
    NGO
}

enum class DiscoveryCategory {
    ALL,
    BAKERY,
    RESTAURANT,
    NOODLES,
    FREE
}

data class UserUiModel(
    val greeting: String,
    val displayName: String,
    val avatarInitials: String
)

//For the pin dropped on the map with its necessary information
data class NearbyDealMarkerUiModel(
    val id: String,
    val labelPrice: String,
    val latitude: Double,
    val longitude: Double
)

data class OfferUiModel(
    val id: String,
    val title: String,
    val storeName: String,
    val imageResId: Int,
    val discountPercent: Int,
    val currentPrice: Double,
    val originalPrice: Double,
    val distanceKm: Double,
    val quantityLeft: Int,
    val hoursToClose: Int,
    val category: DiscoveryCategory,
    val isEligibleForNgoFree: Boolean
)

data class DiscoveryUiState(
    val user: UserUiModel,
    val searchQuery: String,
    val selectedCategory: DiscoveryCategory,
    val availableCategories: List<DiscoveryCategory>,
    val userRole: UserRole,
    val isLoading: Boolean,
    val nearbyMarkers: List<NearbyDealMarkerUiModel>,
    val offers: List<OfferUiModel>,
    val filteredOffers: List<OfferUiModel>
)

