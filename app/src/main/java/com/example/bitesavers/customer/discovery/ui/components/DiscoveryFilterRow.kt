package com.example.bitesavers.customer.discovery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.ui.theme.BiteSaversTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryFilterRow(
    availableCategories: List<DiscoveryCategory>,
    selectedCategory: DiscoveryCategory,
    onCategorySelected: (DiscoveryCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    // LazyRow is perfect here so users can swipe horizontally if the list gets too long!
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(availableCategories) { category ->
            val isSelected = category == selectedCategory

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = getCategoryName(category),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },

                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = null // Removes the default border for a cleaner, modern pill look
            )
        }
    }
}

@Composable
private fun getCategoryName(category: DiscoveryCategory): String {
    return when (category) {
        DiscoveryCategory.ALL -> stringResource(id = R.string.category_all)
        DiscoveryCategory.BAKERY -> stringResource(id = R.string.category_bakery)
        DiscoveryCategory.HOT_MEALS -> stringResource(id = R.string.category_hot_meals)
        DiscoveryCategory.DESSERTS -> stringResource(id = R.string.category_desserts)
        DiscoveryCategory.BEVERAGES -> stringResource(id = R.string.category_beverages)
        DiscoveryCategory.FREE -> stringResource(id = R.string.category_free)
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoveryFilterRowPreview() {
    BiteSaversTheme {
        DiscoveryFilterRow(
            availableCategories = listOf(
                DiscoveryCategory.ALL,
                DiscoveryCategory.BAKERY,
                DiscoveryCategory.HOT_MEALS,
                DiscoveryCategory.DESSERTS,
                DiscoveryCategory.BEVERAGES
            ),
            selectedCategory = DiscoveryCategory.BAKERY,
            onCategorySelected = {}
        )
    }
}