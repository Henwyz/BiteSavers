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
import com.example.bitesavers.customer.orders.OrderSuccessScreen
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
                onReserveSuccess = { offerId, quantity ->
                    navController.navigate(Screen.Checkout.createRoute(offerId, quantity))
                }
            )
        }

        // 6. CHECKOUT SCREEN
        composable(
            route = Screen.Checkout.route,
            arguments = listOf(
                navArgument("offerId") { type = NavType.StringType },
                navArgument("quantity") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId").orEmpty()
            val quantity = backStackEntry.arguments?.getInt("quantity") ?: 1

            CheckoutRoute(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCheckoutSuccess = { orderId ->
                    navController.navigate(Screen.Success.createRoute(orderId)) {
                        popUpTo(Screen.Checkout.route) { inclusive = true }
                    }
                }
            )
        }

        // 7. SUCCESS SCREEN
        composable(
            route = Screen.Success.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()

            OrderSuccessScreen(
                onViewTicketClick = {
                    // Navigates to Ticket screen passing the orderId, and clears Success from history
                    navController.navigate(Screen.Ticket.createRoute(orderId)) {
                        popUpTo(Screen.Success.route) { inclusive = true }
                    }
                }
            )
        }

        // 8. TICKET SCREEN
        composable(
            route = Screen.Ticket.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            // TicketViewModel will automatically pick up this "orderId" argument via SavedStateHandle!
            TicketRoute(
                onNavigateBack = {
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