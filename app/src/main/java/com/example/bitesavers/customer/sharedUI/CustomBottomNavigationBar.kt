package com.example.bitesavers.sharedUI

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bitesavers.R
import com.example.bitesavers.customer.navigation.CustomerScreen
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun CustomerBottomNavigationBar(navController: NavController) {
    // This observes the navigation state to figure out which tab is currently active
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        // DISCOVER TAB (Search icon)
        NavigationBarItem(
            selected = currentRoute == CustomerScreen.Discovery.route,
            onClick = {
                navController.navigate(CustomerScreen.Discovery.route) {
                    popUpTo(CustomerScreen.Discovery.route) { inclusive = false } // Prevents building a huge backstack
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = stringResource(id = R.string.nav_discover),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(text = stringResource(id = R.string.nav_discover)) }
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
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_orders),
                    contentDescription = stringResource(id = R.string.nav_orders),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(text = stringResource(id = R.string.nav_orders)) }
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
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_saved),
                    contentDescription = stringResource(id = R.string.nav_saved),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(text = stringResource(id = R.string.nav_saved)) }
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
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = stringResource(id = R.string.nav_profile),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(text = stringResource(id = R.string.nav_profile)) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomerBottomNavigationBarPreview() {
    BiteSaversTheme {
        CustomerBottomNavigationBar(navController = rememberNavController())
    }
}