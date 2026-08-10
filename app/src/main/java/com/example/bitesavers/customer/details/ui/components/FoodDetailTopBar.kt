package com.example.bitesavers.customer.details.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text = "Offer Details") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                // Using your tutor's advice here!
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Navigate Back"
                )
            }
        },
        windowInsets = WindowInsets(0.dp) // Keeps the spacing clean
    )
}