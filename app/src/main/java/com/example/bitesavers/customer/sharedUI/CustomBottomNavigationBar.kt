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
import com.example.bitesavers.customer.navigation.CustomerScreen

@Composable
fun CustomerBottomNavigationBar(navController: NavController) {
    // This observes the navigation state to figure out which tab is currently active
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        // HOME TAB
        NavigationBarItem(
            selected = currentRoute == CustomerScreen.Discovery.route,
            onClick = {
                navController.navigate(CustomerScreen.Discovery.route) {
                    popUpTo(CustomerScreen.Discovery.route) { inclusive = false } // Prevents building a huge backstack
                    launchSingleTop = true
                }
            },
            icon = { Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Home") },
            label = { Text("Home") }
        )

        // ORDERS TAB
        NavigationBarItem(
            selected = currentRoute == CustomerScreen.Orders.route,
            onClick = {
                navController.navigate(CustomerScreen.Orders.route) {
                    popUpTo(CustomerScreen.Discovery.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(painterResource(id = R.drawable.ic_orders), contentDescription = "Orders") },
            label = { Text("Orders") }
        )

        // SAVED TAB
        NavigationBarItem(
            selected = currentRoute == CustomerScreen.Saved.route,
            onClick = {
                navController.navigate(CustomerScreen.Saved.route) {
                    popUpTo(CustomerScreen.Discovery.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(painterResource(id = R.drawable.ic_saved), contentDescription = "Saved") },
            label = { Text("Saved") }
        )

        // PROFILE TAB
        NavigationBarItem(
            selected = currentRoute == CustomerScreen.Profile.route,
            onClick = {
                navController.navigate(CustomerScreen.Profile.route) {
                    popUpTo(CustomerScreen.Discovery.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(painterResource(id = R.drawable.ic_profile), contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}