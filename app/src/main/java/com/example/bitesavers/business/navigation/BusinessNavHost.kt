package com.example.bitesavers.business.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bitesavers.business.analytics.ui.AnalyticsScreen
import com.example.bitesavers.business.dashboard.logic.DashboardViewModel
import com.example.bitesavers.business.dashboard.ui.BusinessHomeScreen
import com.example.bitesavers.business.dashboard.ui.BusinessOrdersScreen
import com.example.bitesavers.business.inventory.logic.InventoryViewModel
import com.example.bitesavers.business.inventory.ui.AddFoodScreen
import com.example.bitesavers.business.inventory.ui.MyListingScreen
import com.example.bitesavers.business.profile.ui.BusinessProfileScreen
import com.example.bitesavers.business.restaurant.ui.RegisterRestaurantScreen
import com.example.bitesavers.business.sharedUI.BusinessBottomNavigationBar
import com.example.bitesavers.business.temperature.ui.AddBoxScreen
import com.example.bitesavers.business.temperature.ui.TemperatureScreen

@Composable
fun BusinessNavHost(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val inventoryViewModel: InventoryViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check store registration status from ViewModel
    val hasChecked = dashboardViewModel.hasCheckedStore
    val needsRegistration = dashboardViewModel.requiresRegistration

    // Automatically navigate new accounts to the Register Restaurant screen
    LaunchedEffect(hasChecked, needsRegistration) {
        if (hasChecked && needsRegistration && currentRoute != BusinessScreen.RegisterRestaurant.route) {
            navController.navigate(BusinessScreen.RegisterRestaurant.route) {
                popUpTo(BusinessScreen.Home.route) { inclusive = true }
            }
        }
    }

    val showBottomBar = currentRoute in listOf(
        BusinessScreen.Home.route,
        BusinessScreen.Listings.route,
        BusinessScreen.Analytics.route,
        BusinessScreen.Profile.route
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BusinessBottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BusinessScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. HOME TAB
            composable(BusinessScreen.Home.route) {
                val dynamicMetrics by dashboardViewModel
                    .getDynamicMetrics(inventoryViewModel.listings)
                    .collectAsState()

                BusinessHomeScreen(
                    metrics = dynamicMetrics,
                    viewModel = dashboardViewModel,
                    onNavigateToAddFood = { navController.navigate(BusinessScreen.AddFood.route) },
                    onNavigateToListings = {
                        navController.navigate(BusinessScreen.Listings.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        } },
                    onNavigateToAnalytics = {
                        navController.navigate(BusinessScreen.Analytics.route) {
                            popUpTo(navController.graph.startDestinationId) {saveState = true}
                            launchSingleTop = true
                            restoreState = true
                        }},
                    onNavigateToTemperature = {
                        navController.navigate(BusinessScreen.Temperature.route)
                    },
                    onNavigateToOrders = {
                        navController.navigate(BusinessScreen.BusinessOrders.route)
                    }
                )
            }

            // 2. LISTINGS TAB
            composable(BusinessScreen.Listings.route) {
                MyListingScreen(
                    viewModel = inventoryViewModel,
                    onNavigateToAddFood = {
                        inventoryViewModel.selectedItemForEdit = null
                        navController.navigate(BusinessScreen.AddFood.route)
                    },
                    onNavigateToEditFood = { _ ->
                        navController.navigate(BusinessScreen.AddFood.route)
                    }
                )
            }

            // 3. ANALYTICS TAB
            composable(BusinessScreen.Analytics.route) {
                AnalyticsScreen()
            }

            // 4. PROFILE TAB
            composable(BusinessScreen.Profile.route) {
                BusinessProfileScreen(
                    onSignOutClick = onLogout
                )
            }

            // 5. ADD FOOD SCREEN
            composable(BusinessScreen.AddFood.route) {
                AddFoodScreen(
                    viewModel = inventoryViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 6. REGISTER RESTAURANT SCREEN
            composable(BusinessScreen.RegisterRestaurant.route) {
                RegisterRestaurantScreen(
                    onRestaurantRegistered = {
                        navController.navigate(BusinessScreen.Listings.route) {
                            popUpTo(BusinessScreen.RegisterRestaurant.route) { inclusive = true }
                        }
                    }
                )
            }

            // 7. TEMPERATURE MONITOR SCREEN
            composable(BusinessScreen.Temperature.route) {
                TemperatureScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddUnitClick = {
                        navController.navigate(BusinessScreen.AddBox.route)
                    }
                )
            }

            // 8. CHECK CUSTOMER ORDER SCREEN
            composable(BusinessScreen.BusinessOrders.route) {
                // Collect the existing recentOrders flow from your DashboardViewModel
                val orders by dashboardViewModel.recentOrders.collectAsState()

                BusinessOrdersScreen(
                    orders = orders,
                    onOrderClick = { orderId ->
                        // Keep empty for now until verification screen is ready
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(BusinessScreen.AddBox.route) {
                AddBoxScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onUnitAdded = { unitName, unitType ->
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun BusinessPlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}