package com.example.bitesavers.business.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bitesavers.business.analytics.ui.AnalyticsScreen
import com.example.bitesavers.business.inventory.logic.InventoryViewModel
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
                onNavigateToAddFood = {}
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
                    // TODO: wire to Member 3's login route once it exists
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
