package com.example.bitesavers.navigation

import com.example.bitesavers.customer.profile.logic.ProfileViewModel
import com.example.bitesavers.customer.profile.ui.ProfileScreen
import com.example.bitesavers.customer.profile.ui.NgoRegistrationScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.bitesavers.customer.profile.logic.NgoFormMode
import com.example.bitesavers.customer.profile.ui.NgoDetailsScreen
import com.example.bitesavers.customer.profile.ui.NgoDisableConfirmScreen
import com.example.bitesavers.customer.profile.ui.NgoUpdatePendingScreen
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

        // 4. PROFILE TAB
        composable(Screen.Profile.route) { backStackEntry ->
            val profileViewModel: ProfileViewModel = viewModel(backStackEntry)
            ProfileScreen(
                viewModel = profileViewModel,
                onRegisterAsNgoClick = {
                    profileViewModel.startNgoRegistration()
                    navController.navigate(Screen.NgoRegistration.route)
                },
                onViewNgoDetailsClick = {
                    navController.navigate(Screen.NgoDetails.route)
                },
                onSignOutClick = {
                    // TODO: wire to Member 3's login route once it exists
                }
            )
        }

        // NGO REGISTRATION SCREEN (brand-new application)
        composable(Screen.NgoRegistration.route) {
            // Scope to Profile's back stack entry so all NGO-related screens
            // share the same ProfileViewModel.
            val profileEntry = remember(it) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            val profileViewModel: ProfileViewModel = viewModel(profileEntry)
            NgoRegistrationScreen(
                mode = NgoFormMode.REGISTER,
                viewModel = profileViewModel,
                onBackClick = { navController.popBackStack() },
                onSubmitted = { navController.popBackStack() }
            )
        }

        // NGO DETAILS SCREEN (read-only, shown once NGO status is approved)
        composable(Screen.NgoDetails.route) {
            val profileEntry = remember(it) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            val profileViewModel: ProfileViewModel = viewModel(profileEntry)
            NgoDetailsScreen(
                viewModel = profileViewModel,
                onBackClick = { navController.popBackStack() },
                onEditClick = {
                    profileViewModel.startNgoEdit()
                    navController.navigate(Screen.NgoEdit.route)
                },
                onDisableClick = {
                    navController.navigate(Screen.NgoDisableConfirm.route)
                }
            )
        }

        // NGO EDIT SCREEN (reuses the same form UI as registration, different mode)
        composable(Screen.NgoEdit.route) {
            val profileEntry = remember(it) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            val profileViewModel: ProfileViewModel = viewModel(profileEntry)
            NgoRegistrationScreen(
                mode = NgoFormMode.EDIT,
                viewModel = profileViewModel,
                onBackClick = { navController.popBackStack() },
                onSubmitted = { navController.navigate(Screen.NgoUpdatePending.route) }
            )
        }

        // NGO UPDATE PENDING SCREEN
        composable(Screen.NgoUpdatePending.route) {
            NgoUpdatePendingScreen(
                onUnderstoodClick = {
                    // Clears NgoUpdatePending, NgoEdit, and NgoDetails off the
                    // back stack in one go, landing cleanly back on Profile.
                    navController.popBackStack(Screen.Profile.route, inclusive = false)
                }
            )
        }

        // NGO DISABLE CONFIRMATION SCREEN
        composable(Screen.NgoDisableConfirm.route) {
            val profileEntry = remember(it) {
                navController.getBackStackEntry(Screen.Profile.route)
            }
            val profileViewModel: ProfileViewModel = viewModel(profileEntry)
            NgoDisableConfirmScreen(
                viewModel = profileViewModel,
                onCancelClick = { navController.popBackStack() },
                onDisabled = {
                    navController.popBackStack(Screen.Profile.route, inclusive = false)
                }
            )
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