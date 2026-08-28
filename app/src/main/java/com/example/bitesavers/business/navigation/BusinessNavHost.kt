package com.example.bitesavers.business.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bitesavers.business.analytics.ui.AnalyticsScreen
import com.example.bitesavers.business.inventory.logic.InventoryViewModel
import com.example.bitesavers.business.inventory.ui.AddFoodScreen
import com.example.bitesavers.business.inventory.ui.MyListingScreen
import com.example.bitesavers.business.profile.ui.BusinessProfileScreen

@Composable
fun BusinessNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BusinessScreen.Home.route,
        modifier = modifier
    ) {
        // 1. HOME TAB — placeholder. This is Member 2's "Business Inventory
        // System" territory (per the team brief); check with them before
        // building this out further, to avoid duplicating work.
        composable(BusinessScreen.Home.route) {
            BusinessPlaceholderScreen("Business Home Coming Soon")
        }

        // 2. LISTINGS TAB — placeholder, same reason as above.
        composable(BusinessScreen.Listings.route) {
           val inventoryViewModel: InventoryViewModel = viewModel()
            MyListingScreen(
                viewModel = inventoryViewModel,
                onNavigateToAddFood = { navController.navigate("add_food") },
                onNavigateToEditFood = { foodId -> navController.navigate("add_food")}
            )
        }

        // 3. ANALYTICS TAB
        composable(BusinessScreen.Analytics.route) {
            AnalyticsScreen()
        }

        // 4. PROFILE TAB
        composable(BusinessScreen.Profile.route) {
            BusinessProfileScreen(
                onSignOutClick = {
                    // Navigate back to your login route (replace "login_screen" with your actual login route name)
                    navController.navigate("login_screen") {
                        popUpTo("login_screen") { inclusive = true } // Clears the whole app history so they can't press back into the dashboard
                    }
                }
            )
        }

        // 5. ADD FOOD SCREEN
        composable("add_food") {
            val parentEntry = remember(it) { navController.getBackStackEntry(BusinessScreen.Listings.route) }
            val inventoryViewModel: InventoryViewModel = viewModel(parentEntry)

            AddFoodScreen(
                viewModel = inventoryViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. REGISTER RESTAURANT SCREEN
        composable(BusinessScreen.RegisterRestaurant.route) {
            com.example.bitesavers.business.restaurant.ui.RegisterRestaurantScreen(
                onRestaurantRegistered = {
                    // Once saved, navigate back or forward to listings/home
                    navController.navigate(BusinessScreen.Listings.route) {
                        popUpTo(BusinessScreen.RegisterRestaurant.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun BusinessPlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}
