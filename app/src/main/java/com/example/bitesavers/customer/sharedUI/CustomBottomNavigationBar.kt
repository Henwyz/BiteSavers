package com.example.bitesavers.sharedUI

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.bitesavers.R
import com.example.bitesavers.navigation.Screen

@Composable
fun CustomerBottomNavigationBar(navController: NavController) {
    // This observes the navigation state to figure out which tab is currently active
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        // HOME TAB
        NavigationBarItem(
            selected = currentRoute == Screen.Discovery.route,
            onClick = {
                navController.navigate(Screen.Discovery.route) {
                    popUpTo(Screen.Discovery.route) { inclusive = false } // Prevents building a huge backstack
                    launchSingleTop = true
                }
            },
            icon = { Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Home") },
            label = { Text("Home") }
        )

        // ORDERS TAB
        NavigationBarItem(
            selected = currentRoute == Screen.Orders.route,
            onClick = {
                navController.navigate(Screen.Orders.route) {
                    popUpTo(Screen.Discovery.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(painterResource(id = R.drawable.ic_orders), contentDescription = "Orders") },
            label = { Text("Orders") }
        )

        // SAVED TAB
        NavigationBarItem(
            selected = currentRoute == Screen.Saved.route,
            onClick = {
                navController.navigate(Screen.Saved.route) {
                    popUpTo(Screen.Saved.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(painterResource(id = R.drawable.ic_saved), contentDescription = "Orders") },
            label = { Text("Saved") }
        )

        // PROFILE TAB
        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = {
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.Profile.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(painterResource(id = R.drawable.ic_profile), contentDescription = "Orders") },
            label = { Text("Profile") }
        )
    }
}