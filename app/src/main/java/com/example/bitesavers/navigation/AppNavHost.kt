package com.example.bitesavers.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bitesavers.customer.OrderSuccess.OrderSuccessScreen
import com.example.bitesavers.customer.checkout.ui.CheckoutRoute // Imported the Checkout Route
import com.example.bitesavers.customer.details.logic.FoodDetailViewModel
import com.example.bitesavers.customer.details.ui.FoodDetailRoute
import com.example.bitesavers.customer.discovery.ui.DiscoveryRoute
import com.example.bitesavers.customer.ticket.ui.TicketRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Discovery.route,
        modifier = modifier
    ) {
        // 1. HOME / DISCOVERY TAB
        composable(Screen.Discovery.route) {
            DiscoveryRoute(
                onOfferClick = { offerId ->
                    navController.navigate(Screen.FoodDetail.createRoute(offerId))
                }
            )
        }

        // 2. ORDERS TAB (Placeholder for now)
        composable(Screen.Orders.route) {
            PlaceholderScreen("Orders Coming Soon")
        }

        // 3. SAVED TAB (Placeholder for now)
        composable(Screen.Saved.route) {
            PlaceholderScreen("Saved Items Coming Soon")
        }

        // 4. PROFILE TAB (Placeholder for now)
        composable(Screen.Profile.route) {
            PlaceholderScreen("Profile Coming Soon")
        }

        // 5. FOOD DETAIL SCREEN
        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(
                navArgument("offerId") { type = NavType.StringType }
            )
        ) {
            val viewModel: FoodDetailViewModel = viewModel()

            FoodDetailRoute(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onReserveSuccess = {
                    navController.navigate(Screen.Checkout.route)
                }
            )
        }

        // 6. CHECKOUT SCREEN
        composable(Screen.Checkout.route) {
            CheckoutRoute(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCheckoutSuccess = {
                    // Navigates to Success screen and removes Checkout from the backstack
                    navController.navigate(Screen.Success.route) {
                        popUpTo(Screen.Checkout.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Success.route) {
            OrderSuccessScreen(
                onViewTicketClick = {
                    // Navigates to Ticket screen and clears the success screen from history
                    navController.navigate(Screen.Ticket.route) {
                        popUpTo(Screen.Success.route) { inclusive = true }
                    }
                }
            )
        }

        // 7. TICKET SCREEN
        composable(Screen.Ticket.route) {
            TicketRoute(
                onNavigateBack = {
                    // Returns the user to the Discovery screen and clears the history
                    navController.navigate(Screen.Discovery.route) {
                        popUpTo(Screen.Discovery.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}