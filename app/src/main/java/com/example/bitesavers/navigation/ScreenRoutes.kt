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

    object Checkout : Screen("checkout_screen/{offerId}/{quantity}") {
        fun createRoute(offerId: String, quantity: Int) = "checkout_screen/$offerId/$quantity"
    }

    object Success : Screen("success_screen/{orderId}") {
        fun createRoute(orderId: String) = "success_screen/$orderId"
    }

    object Ticket : Screen("ticket_screen/{orderId}") {
        fun createRoute(orderId: String) = "ticket_screen/$orderId"
    }

    object NgoRegistration : Screen("ngo_registration_screen")
    object NgoDetails : Screen("ngo_details_screen")
    object NgoEdit : Screen("ngo_edit_screen")
    object NgoUpdatePending : Screen("ngo_update_pending_screen")
}