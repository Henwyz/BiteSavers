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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaversTheme

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
            // Bookmark / favourite toggle button using painterResource
            IconButton(onClick = onBookmarkClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_saved),
                    contentDescription = stringResource(
                        id = if (isSaved) R.string.cd_bookmark_saved else R.string.cd_bookmark_unsaved
                    ),
                    tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        windowInsets = WindowInsets(0.dp) // Keeps the spacing clean
    )
}

@Preview(showBackground = true, name = "TopBar - Not Saved")
@Composable
private fun FoodDetailTopBarUnsavedPreview() {
    BiteSaversTheme {
        FoodDetailTopBar(
            isSaved = false,
            onBackClick = {},
            onBookmarkClick = {}
        )
    }
}

@Preview(showBackground = true, name = "TopBar - Saved")
@Composable
private fun FoodDetailTopBarSavedPreview() {
    BiteSaversTheme {
        FoodDetailTopBar(
            isSaved = true,
            onBackClick = {},
            onBookmarkClick = {}
        )
    }
}