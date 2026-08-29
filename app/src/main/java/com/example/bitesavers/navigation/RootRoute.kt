package com.example.bitesavers.navigation

sealed class RootRoute(val route: String) {
    data object Login : RootRoute("login_screen")
    data object SignUp : RootRoute("signup_screen")
    data object CustomerGraph : RootRoute("customer_graph")
    data object BusinessGraph : RootRoute("business_graph")
}