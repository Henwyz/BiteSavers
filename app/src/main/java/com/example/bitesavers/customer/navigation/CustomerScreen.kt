package com.example.bitesavers.customer.navigation

sealed class CustomerScreen(val route: String) {
    data object Discovery : CustomerScreen("discovery_screen")
    data object Orders : CustomerScreen("orders_screen")
    data object Saved : CustomerScreen("saved_screen")
    data object Profile : CustomerScreen("profile_screen")

    data object StoreDetail : CustomerScreen("store_detail_screen/{storeId}") {
        fun createRoute(storeId: String) = "store_detail_screen/$storeId"
    }

    data object FoodDetail : CustomerScreen("food_detail/{offerId}") {
        fun createRoute(offerId: String) = "food_detail/$offerId"
    }

    data object Checkout : CustomerScreen("checkout_screen/{offerId}/{quantity}") {
        fun createRoute(offerId: String, quantity: Int) = "checkout_screen/$offerId/$quantity"
    }

    data object Success : CustomerScreen("success_screen/{orderId}") {
        fun createRoute(orderId: String) = "success_screen/$orderId"
    }

    data object Ticket : CustomerScreen("ticket_screen/{orderId}") {
        fun createRoute(orderId: String) = "ticket_screen/$orderId"
    }

    data object PaymentMethods : CustomerScreen("payment_methods_screen")

    data object NgoRegistration : CustomerScreen("ngo_registration_screen")
    data object NgoDetails : CustomerScreen("ngo_details_screen")
    data object NgoEdit : CustomerScreen("ngo_edit_screen")
    data object NgoUpdatePending : CustomerScreen("ngo_update_pending_screen")
    data object NgoDisableConfirm : CustomerScreen("ngo_disable_confirm_screen")

    data object PrivacySecurity : CustomerScreen("privacy_security_screen")
    data object HelpSupport : CustomerScreen("help_support_screen")
    data object AboutBiteSaver : CustomerScreen("about_bitesaver_screen")
}