package com.example.bitesavers.customer.checkout.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaversTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.checkout_top_bar_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(id = R.string.cd_navigate_back)
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun CheckoutTopBarPreview() {
    BiteSaversTheme {
        CheckoutTopBar(
            onBackClick = {}
        )
    }
}