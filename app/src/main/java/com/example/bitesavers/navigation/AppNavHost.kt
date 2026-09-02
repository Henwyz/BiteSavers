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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bitesavers.business.navigation.BusinessNavHost
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
    startDestination: String = remember {
        if (UserSession.isLoggedIn()) {
            val role = UserSession.getUserRole()
            if (role.equals("business", ignoreCase = true)) {
                RootRoute.BusinessGraph.route
            } else {
                RootRoute.CustomerGraph.route
            }
        } else {
            RootRoute.Login.route
        }
    }
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
                    val destination = if (isBusiness) {
                        RootRoute.BusinessGraph.route
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

        // 4. Customer Navigation Subgraph
        composable(RootRoute.CustomerGraph.route) {
            CustomerNavHost(
                onLogout = {
                    navController.navigate(RootRoute.Login.route) {
                        popUpTo(RootRoute.CustomerGraph.route) { inclusive = true }
                    }
                }
            )
        }

        // 5. Business Navigation Subgraph
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