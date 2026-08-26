package com.example.bitesavers.navigation

sealed class Screen(val route: String) {
    object Discovery : Screen("discovery_screen")
    object Orders : Screen("orders_screen")
    object Saved : Screen("saved_screen")
    object Profile : Screen("profile_screen")

    object FoodDetail : Screen("food_detail/{offerId}") {
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

    object PaymentMethods : Screen("payment_methods_screen")

    object NgoRegistration : Screen("ngo_registration_screen")
    object NgoDetails : Screen("ngo_details_screen")
    object NgoEdit : Screen("ngo_edit_screen")
    object NgoUpdatePending : Screen("ngo_update_pending_screen")
    object NgoDisableConfirm : Screen("ngo_disable_confirm_screen")

    object PrivacySecurity : Screen("privacy_security_screen")
    object HelpSupport : Screen("help_support_screen")
    object AboutBiteSaver : Screen("about_bitesaver_screen")
}