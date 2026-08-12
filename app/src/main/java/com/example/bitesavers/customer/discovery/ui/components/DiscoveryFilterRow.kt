package com.example.bitesavers.customer.discovery.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun DiscoveryFilterRow(
    categories: List<DiscoveryCategory>,
    selectedCategory: DiscoveryCategory,
    onCategorySelected: (DiscoveryCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val selected = category == selectedCategory

            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(text = category.toDisplayText())
                },
                colors = FilterChipDefaults.filterChipColors(
                    // UNSELECTED STATE
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,

                    // SELECTED STATE
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                // Removes the default border for a cleaner brand look
                border = null
            )
        }
    }
}

@Composable
private fun DiscoveryCategory.toDisplayText(): String {
    return when (this) {
        DiscoveryCategory.ALL -> stringResource(id = R.string.filter_all)
        DiscoveryCategory.HOT_MEALS -> stringResource(id = R.string.filter_hot_meals)
        DiscoveryCategory.BAKERY -> stringResource(id = R.string.filter_bakery)
        DiscoveryCategory.DESSERTS -> stringResource(id = R.string.filter_desserts)
        DiscoveryCategory.BEVERAGES -> stringResource(id = R.string.filter_beverages)
        DiscoveryCategory.FREE -> stringResource(id = R.string.filter_free)
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoveryFilterRowPreview() {
    BiteSaversTheme {
        DiscoveryFilterRow(
            categories = listOf(
                DiscoveryCategory.ALL,
                DiscoveryCategory.HOT_MEALS,
                DiscoveryCategory.BAKERY,
                DiscoveryCategory.DESSERTS,
                DiscoveryCategory.BEVERAGES,
                DiscoveryCategory.FREE
            ),
            selectedCategory = DiscoveryCategory.ALL,
            onCategorySelected = {}
        )
    }
}