package com.example.bitesavers.customer.discovery.data

import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole

object DiscoveryDummyData {

    val defaultUser = UserUiModel(
        greeting = "👋 Good Evening",
        displayName = "Sarah Tan",
        avatarInitials = "ST"
    )

    val defaultCategories = listOf(
        DiscoveryCategory.ALL,
        DiscoveryCategory.BAKERY,
        DiscoveryCategory.RESTAURANT,
        DiscoveryCategory.NOODLES
    )

    //dummy data for markers
    val defaultMarkers = listOf(
        NearbyDealMarkerUiModel("m1", "RM 9", 3.1390, 101.6869),
        NearbyDealMarkerUiModel("m2", "RM 7", 3.1400, 101.6900),
        NearbyDealMarkerUiModel("m3", "RM 12", 3.1370, 101.6920)
    )

    //dummy data for list of fake food deals
    val defaultOffers = listOf(
        OfferUiModel(
            id = "o1",
            title = "Bolognese Spaghetti",
            storeName = "Mr Lee Western Food",
            imageResId = R.drawable.food_spaghetti,
            discountPercent = 30,
            currentPrice = 10.50,
            originalPrice = 15.00,
            distanceKm = 1.9,
            quantityLeft = 10,
            hoursToClose = 2,
            category = DiscoveryCategory.RESTAURANT,
            isEligibleForNgoFree = false,
            liveTemperature = 65.0,
            storageType = "HOT",
            description = "Extra portions of our signature Bolognese Spaghetti from lunch service. Packed in thermal containers to keep it warm and fresh."
        ),
        OfferUiModel(
            id = "o2",
            title = "Nasi Goreng Ayam",
            storeName = "Nani Kafe",
            imageResId = R.drawable.food_nasi_goreng,
            discountPercent = 40,
            currentPrice = 9.00,
            originalPrice = 15.00,
            distanceKm = 3.2,
            quantityLeft = 6,
            hoursToClose = 1,
            category = DiscoveryCategory.NOODLES,
            isEligibleForNgoFree = false,
            liveTemperature = 62.0,
            storageType = "HOT",
            description = "Authentic Nasi Goreng Ayam cooked with fresh ingredients. A generous portion left over from the evening rush, ready to be rescued!"
        )
    )

    //as for now we use dummy data first
    //but its actually for our Viewmodel to fetches the data when the screen first open up
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