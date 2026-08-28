package com.example.bitesavers.customer.details.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailTopBar(
    isSaved: Boolean = false,
    onBackClick: () -> Unit,
    onBookmarkClick: () -> Unit = {}
) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.detail_top_bar_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(id = R.string.cd_navigate_back)
                )
            }
        },
        actions = {
            IconButton(onClick = onBookmarkClick) {
                Icon(
                    painter = painterResource(
                        id = if (isSaved) R.drawable.ic_launcher_foreground else R.drawable.ic_launcher_foreground // Replace with your bookmark filled/outline drawables
                    ),
                    contentDescription = stringResource(id = R.string.cd_bookmark_icon),
                    tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        windowInsets = WindowInsets(0.dp) // Keeps the spacing clean
    )
}