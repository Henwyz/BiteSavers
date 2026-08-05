package com.example.bitesavers.discovery.data

import com.example.bitesavers.R

object DiscoveryDummyData {

    val defaultUser = UserUiModel(
        greeting = "👋 Good Evening",
        displayName = "Sarah Tan",
        avatarInitials = "SL"
    )

    val defaultCategories = listOf(
        DiscoveryCategory.ALL,
        DiscoveryCategory.BAKERY,
        DiscoveryCategory.RESTAURANT,
        DiscoveryCategory.NOODLES
    )

    val defaultMarkers = listOf(
        NearbyDealMarkerUiModel("m1", "RM 9", 3.1390, 101.6869),
        NearbyDealMarkerUiModel("m2", "RM 7", 3.1400, 101.6900),
        NearbyDealMarkerUiModel("m3", "RM 12", 3.1370, 101.6920)
    )

    val defaultOffers = listOf(
        OfferUiModel(
            id = "o1",
            title = "Bolognese Spaghetti",
            storeName = "Mr Lee Western Food",
            imageResId = R.drawable.food_spaghetti, // replace if your drawable name differs
            discountPercent = 30,
            currentPrice = 10.50,
            originalPrice = 15.00,
            distanceKm = 1.9,
            quantityLeft = 10,
            hoursToClose = 2,
            category = DiscoveryCategory.RESTAURANT,
            isEligibleForNgoFree = false
        ),
        OfferUiModel(
            id = "o2",
            title = "Nasi Goreng Ayam",
            storeName = "Nani Kafe",
            imageResId = R.drawable.food_nasi_goreng, // replace if your drawable name differs
            discountPercent = 40,
            currentPrice = 9.00,
            originalPrice = 15.00,
            distanceKm = 3.2,
            quantityLeft = 6,
            hoursToClose = 1,
            category = DiscoveryCategory.NOODLES,
            isEligibleForNgoFree = false
        )
    )

    fun initialState(): DiscoveryUiState {
        return DiscoveryUiState(
            user = defaultUser,
            searchQuery = "",
            selectedCategory = DiscoveryCategory.ALL,
            availableCategories = defaultCategories,
            userRole = UserRole.CONSUMER,
            isLoading = false,
            nearbyMarkers = defaultMarkers,
            offers = defaultOffers,
            filteredOffers = defaultOffers
        )
    }
}