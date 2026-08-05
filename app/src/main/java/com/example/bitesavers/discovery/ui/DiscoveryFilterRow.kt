package com.example.bitesavers.discovery.ui

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.discovery.data.DiscoveryCategory
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
            //allows user to swipe left and right
            //rememberScrollState() tells compose to remember how far user has scrolled
            //so it doesnt snap back to the beginning when the screen redraws
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val selected = category == selectedCategory

            //built in component for selecting category
            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(text = category.toDisplayText())
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

//Since our enum are all written in caps, this toDisplayText (Extension Function)
//convert the raw code names into readable string
private fun DiscoveryCategory.toDisplayText(): String {
    return when (this) {
        DiscoveryCategory.ALL -> "All"
        DiscoveryCategory.BAKERY -> "Bakery"
        DiscoveryCategory.RESTAURANT -> "Restaurant"
        DiscoveryCategory.NOODLES -> "Noodles"
        DiscoveryCategory.FREE -> "Free"
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoveryFilterRowPreview() {
    BiteSaversTheme {
        DiscoveryFilterRow(
            categories = listOf(
                DiscoveryCategory.ALL,
                DiscoveryCategory.BAKERY,
                DiscoveryCategory.RESTAURANT,
                DiscoveryCategory.NOODLES
            ),
            selectedCategory = DiscoveryCategory.ALL,
            onCategorySelected = {}
        )
    }
}