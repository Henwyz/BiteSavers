package com.example.bitesavers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue // Needed for by
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState // Needed to track the current screen
import androidx.navigation.compose.rememberNavController
import com.example.bitesavers.navigation.AppNavHost
import com.example.bitesavers.navigation.Screen // Import your Screen routes
import com.example.bitesavers.sharedUI.CustomerBottomNavigationBar
import com.example.bitesavers.ui.theme.BiteSaversTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiteSaversTheme {
                val navController = rememberNavController()

                // 1. Observe the current route the user is on
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 2. Make a list of screens that are allowed to have the bottom bar
                val bottomBarRoutes = listOf(
                    Screen.Discovery.route,
                    Screen.Orders.route,
                    Screen.Saved.route,
                    Screen.Profile.route
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // 3. Only draw the bottom bar if the current screen is in our list!
                        if (currentRoute in bottomBarRoutes) {
                            CustomerBottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}