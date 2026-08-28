package com.example.bitesavers.business.navigation

sealed class BusinessScreen(val route: String) {
    object Home : BusinessScreen("business_home_screen")
    object Listings : BusinessScreen("business_listings_screen")
    object Analytics : BusinessScreen("business_analytics_screen")
    object Profile : BusinessScreen("business_profile_screen")
    object RegisterRestaurant : BusinessScreen("register_restaurant_screen")
}
