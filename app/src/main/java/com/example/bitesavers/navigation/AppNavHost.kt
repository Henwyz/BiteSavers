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
import com.example.bitesavers.customer.details.logic.FoodDetailViewModel
import com.example.bitesavers.customer.details.ui.FoodDetailRoute // Updated to use the Route
import com.example.bitesavers.customer.discovery.ui.DiscoveryRoute
import com.example.bitesavers.customer.ticket.ui.TicketRoute // Imported the new Ticket Route

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
            route = Screen.FoodDetail.route, // Using your sealed class!
            arguments = listOf(
                navArgument("offerId") { type = NavType.StringType }
            )
        ) {
            val viewModel: FoodDetailViewModel = viewModel()

            // Using the Route wrapper we built so it handles the UiEvents perfectly
            FoodDetailRoute(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onReserveSuccess = {
                    // THE JUMP: Now navigates to the ticket screen instead of popping back
                    navController.navigate(Screen.Ticket.route)
                }
            )
        }

        // 6. TICKET SCREEN
        composable(Screen.Ticket.route) {
            TicketRoute(
                onNavigateBack = {
                    // Returns the user to the Discovery screen and clears the history
                    // so pressing the physical back button on the phone doesn't
                    // accidentally reopen the receipt.
                    navController.navigate(Screen.Discovery.route) {
                        popUpTo(Screen.Discovery.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

// A quick temporary screen to prove our navigation works!
@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}