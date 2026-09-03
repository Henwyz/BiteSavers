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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bitesavers.business.analytics.ui.AnalyticsScreen
import com.example.bitesavers.business.dashboard.logic.DashboardViewModel
import com.example.bitesavers.business.dashboard.ui.BusinessCancelScreen
import com.example.bitesavers.business.dashboard.ui.BusinessHomeScreen
import com.example.bitesavers.business.dashboard.ui.BusinessOrdersScreen
import com.example.bitesavers.business.dashboard.ui.BusinessVerificationScreen
import com.example.bitesavers.business.inventory.logic.InventoryViewModel
import com.example.bitesavers.business.inventory.ui.AddFoodScreen
import com.example.bitesavers.business.inventory.ui.MyListingScreen
import com.example.bitesavers.business.profile.logic.BusinessProfileViewModel
import com.example.bitesavers.business.profile.ui.BusinessProfileEditScreen
import com.example.bitesavers.business.profile.ui.BusinessProfileScreen
import com.example.bitesavers.business.profile.ui.BusinessUpdatePendingScreen
import com.example.bitesavers.business.restaurant.ui.RegisterRestaurantScreen
import com.example.bitesavers.business.sharedUI.BusinessBottomNavigationBar
import com.example.bitesavers.business.temperature.logic.TemperatureViewModel
import com.example.bitesavers.business.temperature.ui.AddBoxScreen
import com.example.bitesavers.business.temperature.ui.TemperatureScreen
import com.example.bitesavers.business.dashboard.ui.BusinessNotificationScreen
import com.example.bitesavers.business.payment.logic.MerchantWalletViewModel
import com.example.bitesavers.business.wallet.ui.MerchantWalletScreen

@Composable
fun BusinessNavHost(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val inventoryViewModel: InventoryViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()
    val temperatureViewModel: TemperatureViewModel = viewModel()
    val businessProfileViewModel: BusinessProfileViewModel = viewModel()

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
                    onNavigateToNotifications = { navController.navigate(BusinessScreen.Notification.route) },
                    onNavigateToAddFood = { navController.navigate(BusinessScreen.AddFood.route) },
                    onNavigateToListings = {
                        navController.navigate(BusinessScreen.Listings.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAnalytics = {
                        navController.navigate(BusinessScreen.Analytics.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToTemperature = {
                        navController.navigate(BusinessScreen.Temperature.route)
                    },
                    onNavigateToOrders = {
                        navController.navigate(BusinessScreen.BusinessOrders.route)
                    },

                    onNavigateToVerification = { orderId ->
                        navController.navigate(BusinessScreen.Verification.createRoute(orderId))
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
                    viewModel = businessProfileViewModel,
                    onSignOutClick = onLogout,
                    onEditClick = {
                        businessProfileViewModel.initEditScreen()
                        navController.navigate(BusinessScreen.EditProfile.route)
                    },
                    onViewWalletClick = { storeId ->
                        val targetId = if (storeId.isNotBlank()) storeId else (dashboardViewModel.currentStoreId ?: "")
                        navController.navigate(BusinessScreen.Wallet.createRoute(targetId))
                    }
                )
            }

            // 5. EDIT PROFILE SCREEN
            composable(BusinessScreen.EditProfile.route) {
                BusinessProfileEditScreen(
                    viewModel = businessProfileViewModel, // 👈 Uses shared instance with pre-filled data
                    onBackClick = { navController.popBackStack() },
                    onSubmitted = { isBusinessDetails ->
                        if (isBusinessDetails) {
                            navController.navigate(BusinessScreen.UpdatePending.route) {
                                popUpTo(BusinessScreen.Profile.route)
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(
                route = BusinessScreen.Wallet.route,
                arguments = listOf(navArgument("storeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val storeId = backStackEntry.arguments?.getString("storeId").orEmpty()
                val walletViewModel: MerchantWalletViewModel = viewModel()

                MerchantWalletScreen(
                    viewModel = walletViewModel,
                    storeId = storeId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 6. UPDATE PENDING SCREEN
            composable(BusinessScreen.UpdatePending.route) {
                BusinessUpdatePendingScreen(
                    onUnderstoodClick = {
                        navController.popBackStack(BusinessScreen.Profile.route, inclusive = false)
                    }
                )
            }

            // 7. ADD FOOD SCREEN
            composable(BusinessScreen.AddFood.route) {
                AddFoodScreen(
                    viewModel = inventoryViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 8. REGISTER RESTAURANT SCREEN
            composable(BusinessScreen.RegisterRestaurant.route) {
                RegisterRestaurantScreen(
                    onRestaurantRegistered = {
                        navController.navigate(BusinessScreen.Listings.route) {
                            popUpTo(BusinessScreen.RegisterRestaurant.route) { inclusive = true }
                        }
                    }
                )
            }

            // 9. TEMPERATURE MONITOR SCREEN
            composable(BusinessScreen.Temperature.route) {
                val currentStoreId = dashboardViewModel.currentStoreId ?: ""

                LaunchedEffect(currentStoreId) {
                    if (currentStoreId.isNotBlank()) {
                        temperatureViewModel.fetchUnitsForStore(currentStoreId)
                    }
                }

                TemperatureScreen(
                    viewModel = temperatureViewModel,
                    storeId = currentStoreId,
                    onBackClick = { navController.popBackStack() },
                    onAddUnitClick = {
                        navController.navigate(BusinessScreen.AddBox.route)
                    }
                )
            }

            // 10. ADD BOX SCREEN (Single, clean route handling (sensorId, isHotBox))
            composable(BusinessScreen.AddBox.route) {
                var errorMessage by remember { mutableStateOf("") }
                val context = LocalContext.current

                AddBoxScreen(
                    errorMessage = errorMessage,
                    onNavigateBack = { navController.popBackStack() },
                    onUnitAdded = { sensorId, isHotBox ->
                        val currentStoreId = dashboardViewModel.currentStoreId ?: ""
                        errorMessage = "" // Clear previous error

                        temperatureViewModel.addNewBox(
                            storeId = currentStoreId,
                            sensorCodeInput = sensorId,
                            isHotBox = isHotBox,
                            onError = { errorResId ->
                                errorMessage = context.resources.getString(errorResId)
                            },
                            onSuccess = {
                                navController.popBackStack()
                            }
                        )
                    }
                )
            }

            // 11. CHECK CUSTOMER ORDER SCREEN
            composable(BusinessScreen.BusinessOrders.route) {
                // Collect the existing recentOrders flow from your DashboardViewModel
                val orders by dashboardViewModel.recentOrders.collectAsState()

                BusinessOrdersScreen(
                    orders = orders,
                    onOrderClick = { orderId ->
                        navController.navigate(BusinessScreen.Verification.createRoute(orderId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 12. Business Verification Check
            composable(
                route = BusinessScreen.Verification.route,
                arguments = listOf(
                    navArgument("orderId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()

                BusinessVerificationScreen(
                    orderId = orderId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToCancel = { targetOrderId ->
                        navController.navigate(BusinessScreen.CancelOrder.createRoute(targetOrderId))
                    },
                    onVerificationCompleted = {
                        // Pop back to refresh and display order status
                        dashboardViewModel.fetchOrdersFromSupabase()
                        navController.popBackStack()
                    }
                )
            }

            // 13. Business Cancel Order
            composable(
                route = BusinessScreen.CancelOrder.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()

                BusinessCancelScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() },
                    onCancelCompleted = {
                        // Refresh orders and pop back to order lists
                        dashboardViewModel.fetchOrdersFromSupabase()
                        navController.popBackStack(BusinessScreen.BusinessOrders.route, inclusive = false)
                    }
                )
            }

            // 14. BUSINESS NOTIFICATION SCREEN
            composable(BusinessScreen.Notification.route) {
                BusinessNotificationScreen(
                    viewModel = dashboardViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOrderDetails = { orderId ->
                        navController.navigate(BusinessScreen.Verification.createRoute(orderId))
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