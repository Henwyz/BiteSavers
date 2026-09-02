package com.example.bitesavers.business.navigation

sealed class BusinessScreen(val route: String) {
    object Home : BusinessScreen("business_home_screen")
    object Listings : BusinessScreen("business_listings_screen")
    object AddFood : BusinessScreen("business_add_food_screen")
    object Analytics : BusinessScreen("business_analytics_screen")
    object Profile : BusinessScreen("business_profile_screen")
    object RegisterRestaurant : BusinessScreen("register_restaurant_screen")
    object Temperature : BusinessScreen("temperature_screen")
    object AddBox : BusinessScreen("add_box_screen")
    object BusinessOrders : BusinessScreen("business_orders")

    object Verification : BusinessScreen("business_verification/{orderId}") {
        // Generates the destination route filled with the concrete order ID
        fun createRoute(orderId: String): String = "business_verification/$orderId"
    }
}
