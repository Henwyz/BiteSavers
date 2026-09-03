package com.example.bitesavers.business.navigation

sealed class BusinessScreen(val route: String) {
    object Home : BusinessScreen("business_home_screen")
    object Listings : BusinessScreen("business_listings_screen")
    object AddFood : BusinessScreen("business_add_food_screen")
    object Analytics : BusinessScreen("business_analytics_screen")
    object Profile : BusinessScreen("business_profile_screen")
    object RegisterRestaurant : BusinessScreen("register_restaurant_screen")
    data object EditProfile : BusinessScreen("business_edit_profile")
    data object UpdatePending : BusinessScreen("business_update_pending")
    object Temperature : BusinessScreen("temperature_screen")
    object AddBox : BusinessScreen("add_box_screen")

    object BusinessOrders : BusinessScreen("business_orders")
}
/*
    data object Home : BusinessScreen("business_home")
    data object Listings : BusinessScreen("business_listings")
    data object Analytics : BusinessScreen("business_analytics")
    data object Profile : BusinessScreen("business_profile")
    data object AddFood : BusinessScreen("business_add_food")
    data object RegisterRestaurant : BusinessScreen("business_register_restaurant")
    data object EditProfile : BusinessScreen("business_edit_profile")

    data object UpdatePending : BusinessScreen("business_update_pending")
}
*/
