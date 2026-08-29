package com.example.bitesavers.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bitesavers.business.navigation.BusinessNavHost
import com.example.bitesavers.customer.navigation.CustomerNavHost
import com.example.bitesavers.login.ui.LoginScreen
import com.example.bitesavers.login.ui.SignUpScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = RootRoute.Login.route
) {
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
                    // Navigate to terms screen or web url if needed
                }
            )
        }

        // 3. Customer Navigation Subgraph
        composable(RootRoute.CustomerGraph.route) {
            CustomerNavHost(
                onLogout = {
                    navController.navigate(RootRoute.Login.route) {
                        popUpTo(RootRoute.CustomerGraph.route) { inclusive = true }
                    }
                }
            )
        }

        // 4. Business Navigation Subgraph
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