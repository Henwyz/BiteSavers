package com.example.bitesavers.customer.discovery.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource // NEW IMPORT
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun DiscoverySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        // UPDATED TO USE STRING RESOURCE!
        placeholder = { Text(stringResource(id = R.string.search_hint_food_items)) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                // UPDATED TO USE STRING RESOURCE!
                contentDescription = stringResource(id = R.string.cd_search)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun DiscoverySearchBarPreview() {
    BiteSaversTheme {
        DiscoverySearchBar(
            query = "",
            onQueryChange = {}
        )
    }
}