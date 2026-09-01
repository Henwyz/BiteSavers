package com.example.bitesavers.customer.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bitesavers.customer.checkout.ui.CheckoutRoute
import com.example.bitesavers.customer.details.logic.FoodDetailViewModel
import com.example.bitesavers.customer.details.ui.FoodDetailRoute
import com.example.bitesavers.customer.discovery.ui.DiscoveryRoute
import com.example.bitesavers.customer.orders.ui.OrdersRoute
import com.example.bitesavers.customer.payment.ui.PaymentMethodsRoute
import com.example.bitesavers.customer.profile.logic.NgoFormMode
import com.example.bitesavers.customer.profile.logic.ProfileViewModel
import com.example.bitesavers.customer.profile.ui.AboutBiteSaverScreen
import com.example.bitesavers.customer.profile.ui.HelpSupportScreen
import com.example.bitesavers.customer.profile.ui.NgoDetailsScreen
import com.example.bitesavers.customer.profile.ui.NgoDisableConfirmScreen
import com.example.bitesavers.customer.profile.ui.NgoRegistrationScreen
import com.example.bitesavers.customer.profile.ui.NgoUpdatePendingScreen
import com.example.bitesavers.customer.profile.ui.PrivacySecurityScreen
import com.example.bitesavers.customer.profile.ui.ProfileScreen
import com.example.bitesavers.customer.saved.ui.SavedRoute
import com.example.bitesavers.customer.success.OrderSuccessScreen
import com.example.bitesavers.customer.ticket.ui.TicketRoute
import com.example.bitesavers.sharedUI.CustomerBottomNavigationBar

@Composable
fun CustomerNavHost(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Observes the current active screen destination
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define top-level destinations that should display the bottom navigation bar
    val showBottomBar = currentRoute in listOf(
        CustomerScreen.Discovery.route,
        CustomerScreen.Orders.route,
        CustomerScreen.Saved.route,
        CustomerScreen.Profile.route
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                CustomerBottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CustomerScreen.Discovery.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. HOME / DISCOVERY TAB
            composable(CustomerScreen.Discovery.route) {
                DiscoveryRoute(
                    onOfferClick = { offerId ->
                        navController.navigate(CustomerScreen.FoodDetail.createRoute(offerId))
                    }
                )
            }

            // 2. ORDERS TAB
            composable(CustomerScreen.Orders.route) {
                OrdersRoute(
                    onOrderClick = { orderId ->
                        navController.navigate(CustomerScreen.Ticket.createRoute(orderId))
                    }
                )
            }

            // 3. SAVED TAB
            composable(CustomerScreen.Saved.route) {
                SavedRoute(
                    onNavigateToDetail = { offerId ->
                        navController.navigate(CustomerScreen.FoodDetail.createRoute(offerId))
                    }
                )
            }

            // 4. PROFILE TAB
            composable(CustomerScreen.Profile.route) { backStackEntry ->
                val profileViewModel: ProfileViewModel = viewModel(backStackEntry)
                ProfileScreen(
                    viewModel = profileViewModel,
                    onRegisterAsNgoClick = {
                        profileViewModel.startNgoRegistration()
                        navController.navigate(CustomerScreen.NgoRegistration.route)
                    },
                    onViewNgoDetailsClick = {
                        navController.navigate(CustomerScreen.NgoDetails.route)
                    },
                    onSignOutClick = onLogout,
                    onPrivacySecurityClick = { navController.navigate(CustomerScreen.PrivacySecurity.route) },
                    onHelpSupportClick = { navController.navigate(CustomerScreen.HelpSupport.route) },
                    onAboutClick = { navController.navigate(CustomerScreen.AboutBiteSaver.route) },
                    onPaymentMethodsClick = { navController.navigate(CustomerScreen.PaymentMethods.route) }
                )
            }

            composable(CustomerScreen.PrivacySecurity.route) {
                PrivacySecurityScreen(onBackClick = { navController.popBackStack() })
            }
            composable(CustomerScreen.HelpSupport.route) {
                HelpSupportScreen(onBackClick = { navController.popBackStack() })
            }
            composable(CustomerScreen.AboutBiteSaver.route) {
                AboutBiteSaverScreen(onBackClick = { navController.popBackStack() })
            }

            // PAYMENT METHODS SCREEN
            composable(CustomerScreen.PaymentMethods.route) {
                PaymentMethodsRoute(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // NGO REGISTRATION SCREEN
            composable(CustomerScreen.NgoRegistration.route) {
                val profileEntry = remember(it) {
                    navController.getBackStackEntry(CustomerScreen.Profile.route)
                }
                val profileViewModel: ProfileViewModel = viewModel(profileEntry)
                NgoRegistrationScreen(
                    mode = NgoFormMode.REGISTER,
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onSubmitted = { navController.popBackStack() }
                )
            }

            // NGO DETAILS SCREEN
            composable(CustomerScreen.NgoDetails.route) {
                val profileEntry = remember(it) {
                    navController.getBackStackEntry(CustomerScreen.Profile.route)
                }
                val profileViewModel: ProfileViewModel = viewModel(profileEntry)
                NgoDetailsScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = {
                        profileViewModel.startNgoEdit()
                        navController.navigate(CustomerScreen.NgoEdit.route)
                    },
                    onDisableClick = {
                        navController.navigate(CustomerScreen.NgoDisableConfirm.route)
                    }
                )
            }

            // NGO EDIT SCREEN
            composable(CustomerScreen.NgoEdit.route) {
                val profileEntry = remember(it) {
                    navController.getBackStackEntry(CustomerScreen.Profile.route)
                }
                val profileViewModel: ProfileViewModel = viewModel(profileEntry)
                NgoRegistrationScreen(
                    mode = NgoFormMode.EDIT,
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onSubmitted = { navController.navigate(CustomerScreen.NgoUpdatePending.route) }
                )
            }

            // NGO UPDATE PENDING SCREEN
            composable(CustomerScreen.NgoUpdatePending.route) {
                NgoUpdatePendingScreen(
                    onUnderstoodClick = {
                        navController.popBackStack(CustomerScreen.Profile.route, inclusive = false)
                    }
                )
            }

            // NGO DISABLE CONFIRMATION SCREEN
            composable(CustomerScreen.NgoDisableConfirm.route) {
                val profileEntry = remember(it) {
                    navController.getBackStackEntry(CustomerScreen.Profile.route)
                }
                val profileViewModel: ProfileViewModel = viewModel(profileEntry)
                NgoDisableConfirmScreen(
                    viewModel = profileViewModel,
                    onCancelClick = { navController.popBackStack() },
                    onDisabled = {
                        navController.popBackStack(CustomerScreen.Profile.route, inclusive = false)
                    }
                )
            }

            // 5. FOOD DETAIL SCREEN
            composable(
                route = CustomerScreen.FoodDetail.route,
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
                        navController.navigate(CustomerScreen.Checkout.createRoute(offerId, quantity))
                    }
                )
            }

            // 6. CHECKOUT SCREEN
            composable(
                route = CustomerScreen.Checkout.route,
                arguments = listOf(
                    navArgument("offerId") { type = NavType.StringType },
                    navArgument("quantity") { type = NavType.IntType; defaultValue = 1 }
                )
            ) { backStackEntry ->
                val offerId = backStackEntry.arguments?.getString("offerId")
                val quantity = backStackEntry.arguments?.getInt("quantity") ?: 1

                CheckoutRoute(
                    offerId = offerId,
                    quantity = quantity,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPaymentMethods = {
                        navController.navigate(CustomerScreen.PaymentMethods.route)
                    },
                    onCheckoutSuccess = { orderId ->
                        navController.navigate(CustomerScreen.Success.createRoute(orderId)) {
                            popUpTo(CustomerScreen.Checkout.route) { inclusive = true }
                        }
                    }
                )
            }

            // 7. SUCCESS SCREEN
            composable(
                route = CustomerScreen.Success.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()

                OrderSuccessScreen(
                    onViewTicketClick = {
                        navController.navigate(CustomerScreen.Ticket.createRoute(orderId)) {
                            popUpTo(CustomerScreen.Discovery.route) { inclusive = false }
                        }
                    }
                )
            }

            // 8. TICKET SCREEN
            composable(
                route = CustomerScreen.Ticket.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) {
                TicketRoute(
                    onNavigateBack = {
                        navController.navigate(CustomerScreen.Discovery.route) {
                            popUpTo(CustomerScreen.Discovery.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}