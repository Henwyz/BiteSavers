package com.example.bitesavers.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bitesavers.LogIn.ui.LoginScreen
import com.example.bitesavers.LogIn.ui.SignUpScreen
import com.example.bitesavers.business.navigation.BusinessNavHost
import com.example.bitesavers.customer.navigation.CustomerNavHost

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
                    // Navigate back to Login upon registration
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
            val businessNavController = rememberNavController()
            androidx.compose.material3.Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    com.example.bitesavers.business.sharedUI.BusinessBottomNavigationBar(
                        navController = businessNavController
                    )
                }
            ) { innerPadding ->
                BusinessNavHost(
                    navController = businessNavController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}