package com.example.bitesavers.navigation

// Using a sealed class is the safest way to handle Compose Navigation
sealed class Screen(val route: String) {
    object Discovery : Screen("discovery_screen")
    object Orders : Screen("orders_screen")
    object Saved : Screen("saved_screen")
    object Profile : Screen("profile_screen")

    object FoodDetail : Screen("food_detail/{offerId}") {
        // A handy function to build the final string when we actually click a card
        fun createRoute(offerId: String) = "food_detail/$offerId"
    }

    // NEW: Added the Ticket Screen!
    object Ticket : Screen("ticket_screen")
}