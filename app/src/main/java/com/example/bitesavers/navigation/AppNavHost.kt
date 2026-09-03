package com.example.bitesavers.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bitesavers.business.navigation.BusinessNavHost
import com.example.bitesavers.business.restaurant.logic.PendingScreenViewModel
import com.example.bitesavers.business.restaurant.logic.RegisterRestaurantViewModel
import com.example.bitesavers.business.restaurant.ui.PendingApprovalScreen
import com.example.bitesavers.business.restaurant.ui.RegisterRestaurantScreen
import com.example.bitesavers.customer.navigation.CustomerNavHost
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.login.ui.LoginScreen
import com.example.bitesavers.login.ui.SignUpScreen
import com.example.bitesavers.login.ui.TermsScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    // Determine initial destination dynamically from saved disk session
    startDestination: String = RootRoute.Login.route
) {
    val context = LocalContext.current

    // Handles result from the Android 13+ runtime permission prompt
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Result handled; system remembers user's choice
    }

    // Prompts once at root level on app start for both customer and business roles
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 1. Login Screen
        composable(RootRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = { isBusiness ->
                    val userRole = UserSession.getUserRole()

                    val destination = if (userRole.equals("business", ignoreCase = true)) {
                        when (UserSession.getStoreStatus()) {
                            "APPROVED" -> RootRoute.BusinessGraph.route
                            "UNREGISTERED" -> RootRoute.RegisterRestaurant.route
                            else -> RootRoute.PendingApproval.route // PENDING or default
                        }
                    } else {
                        RootRoute.CustomerGraph.route
                    }

                    navController.navigate(destination) {
                        popUpTo(RootRoute.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(RootRoute.SignUp.route)
                }
            )
        }

        // 2. Sign Up Screen
        composable(RootRoute.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(RootRoute.Login.route) {
                        popUpTo(RootRoute.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToTerms = {
                    navController.navigate(RootRoute.Terms.route)
                }
            )
        }

        // 3. Terms & Privacy Policy Screen
        composable(RootRoute.Terms.route) {
            TermsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 4. Restaurant Registration Screen
        composable(RootRoute.RegisterRestaurant.route) {
            // 👇 ADD THIS: Scope the ViewModel to the NavBackStackEntry so it survives recompositions
            val parentEntry = remember(it) { navController.getBackStackEntry(RootRoute.RegisterRestaurant.route) }
            val viewModel: RegisterRestaurantViewModel = viewModel(parentEntry)

            RegisterRestaurantScreen(
                viewModel = viewModel, // 👈 PASS IT EXPLICITLY HERE
                onNavigateBack = {
                    navController.navigate(RootRoute.Login.route) {
                        popUpTo(RootRoute.RegisterRestaurant.route) { inclusive = true }
                    }
                },
                onRestaurantRegistered = {
                    navController.navigate(RootRoute.PendingApproval.route) {
                        popUpTo(RootRoute.RegisterRestaurant.route) { inclusive = true }
                    }
                }
            )
        }

        // 5. Pending Approval Screen
        composable(RootRoute.PendingApproval.route) {
            val viewModel: PendingScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

            PendingApprovalScreen(
                restaurantName = viewModel.restaurantName,
                ssmNumber = viewModel.ssmNumber,
                contactPhone = viewModel.contactPhone,
                address = viewModel.address,
                openingTime = viewModel.openingTime,
                closingTime = viewModel.closingTime,
                cleanupEndTime = viewModel.cleanupEndTime,
                ssmDocUploaded = viewModel.ssmDocUploaded,
                onNavigateBack = {
                    navController.navigate(RootRoute.Login.route) {
                        popUpTo(RootRoute.PendingApproval.route) { inclusive = true }
                    }
                }
            )
        }

        // 6. Customer Navigation Subgraph
        composable(RootRoute.CustomerGraph.route) {
            CustomerNavHost(
                onLogout = {
                    navController.navigate(RootRoute.Login.route) {
                        popUpTo(RootRoute.CustomerGraph.route) { inclusive = true }
                    }
                }
            )
        }

        // 7. Business Navigation Subgraph
        composable(RootRoute.BusinessGraph.route) {
            BusinessNavHost(
                onLogout = {
                    navController.navigate(RootRoute.Login.route) {
                        popUpTo(RootRoute.BusinessGraph.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

