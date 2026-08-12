package com.example.bitesavers.customer.discovery.data

import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole

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


data class DiscoveryUiState(
    val user: UserUiModel,
    val searchQuery: String,
    val selectedCategory: DiscoveryCategory,
    val availableCategories: List<DiscoveryCategory>,
    val userRole: UserRole,
    val isLoading: Boolean,
    val nearbyMarkers: List<NearbyDealMarkerUiModel>,
    val offers: List<OfferUiModel>,
    val filteredOffers: List<OfferUiModel>,
    val selectedMapOfferId: String? = null
)

