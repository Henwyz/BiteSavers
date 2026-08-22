package com.example.bitesavers.business.sharedUI

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.bitesavers.R
import com.example.bitesavers.business.navigation.BusinessScreen

private data class BusinessNavItem(val route: String, val icon: ImageVector, val label: String)

/**
 * NOTE: styled independently from CustomerBottomNavigationBar since I don't
 * have that file's exact content — check the colors/spacing line up
 * visually with it before merging. Both should ultimately look consistent
 * since they share the same MaterialTheme.colorScheme tokens.
 */
@Composable
fun BusinessBottomNavigationBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val items = listOf(
        BusinessNavItem(BusinessScreen.Home.route, Icons.Filled.Storefront, stringResource(R.string.nav_home)),
        BusinessNavItem(BusinessScreen.Listings.route, Icons.Filled.Inventory2, stringResource(R.string.nav_listings)),
        BusinessNavItem(BusinessScreen.Analytics.route, Icons.Filled.BarChart, stringResource(R.string.nav_analytics)),
        BusinessNavItem(BusinessScreen.Profile.route, Icons.Filled.Person, stringResource(R.string.nav_profile))
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
