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
import com.example.bitesavers.customer.details.ui.FoodDetailScreen
import com.example.bitesavers.customer.discovery.ui.DiscoveryRoute

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

        composable(
            route = "food_detail/{offerId}",
            arguments = listOf(
                navArgument("offerId") { type = NavType.StringType }
            )
        ) {
            val viewModel: FoodDetailViewModel = viewModel()

            FoodDetailScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack() // Go back
                },
                onReserveSuccess = {
                    navController.popBackStack() // Go back after success
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