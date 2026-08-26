package com.example.bitesavers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue // Needed for by
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState // Needed to track the current screen
import androidx.navigation.compose.rememberNavController
import com.example.bitesavers.business.navigation.BusinessNavHost
import com.example.bitesavers.business.sharedUI.BusinessBottomNavigationBar
import com.example.bitesavers.navigation.AppNavHost
import com.example.bitesavers.navigation.Screen // Import your Screen routes
import com.example.bitesavers.sharedUI.CustomerBottomNavigationBar
import com.example.bitesavers.ui.theme.BiteSaversTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiteSaversTheme {
                //dead code, refined in the below version
                /*
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
                */

                //start of temp add
                // TEMP: there's no login/role routing yet (that's Member 3's
                // Auth module). Once it exists, replace this with the real
                // logged-in user's role instead of a hardcoded toggle —
                // check with Member 3 before merging this file, since
                // they'll likely need to touch this exact spot too.
                var isBusinessMode by remember { mutableStateOf(true) }

                if (isBusinessMode) {
                    val businessNavController = rememberNavController()
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { BusinessBottomNavigationBar(navController = businessNavController) }
                    ) { innerPadding ->
                        BusinessNavHost(
                            navController = businessNavController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                } else {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val bottomBarRoutes = listOf(
                        Screen.Discovery.route,
                        Screen.Orders.route,
                        Screen.Saved.route,
                        Screen.Profile.route
                    )
//end of temp add

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
}