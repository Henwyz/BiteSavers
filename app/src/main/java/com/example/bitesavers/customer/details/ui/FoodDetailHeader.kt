package com.example.bitesavers.customer.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import java.util.Locale

@Composable
fun FoodDetailHeader(
    offer: OfferUiModel,
    modifier: Modifier = Modifier
) {
    val savedAmount = offer.originalPrice - offer.currentPrice

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // LEFT SIDE: Title and Category
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = offer.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Category: ${offer.category.name.lowercase().replaceFirstChar { it.titlecase(Locale.US) }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // RIGHT SIDE: Pricing and Saved Amount
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Strikethrough original price
            Text(
                text = "RM ${offer.originalPrice.toPrice()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough
            )
            // Current discounted price
            Text(
                text = "RM ${offer.currentPrice.toPrice()}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            // Saved amount text
            Text(
                text = "Saved RM${savedAmount.toPrice()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun Double.toPrice(): String = String.format(Locale.US, "%.2f", this)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun FoodDetailHeaderPreview() {
    com.example.bitesavers.ui.theme.BiteSaversTheme {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailHeader(
                offer = OfferUiModel(
                    id = "c1",
                    title = "Butter Croissant",
                    storeName = "Artisan Bakery",
                    imageResId = com.example.bitesavers.R.drawable.food_spaghetti, // Swap with your actual dummy image
                    discountPercent = 50,
                    currentPrice = 2.50,
                    originalPrice = 5.00,
                    distanceKm = 1.1,
                    quantityLeft = 6,
                    hoursToClose = 1,
                    category = DiscoveryCategory.BAKERY,
                    isEligibleForNgoFree = false,
                    // Added the missing fields here for the preview!
                    liveTemperature = 22.0,
                    storageType = "COLD",
                    description = "Freshly baked butter croissants, perfect for a quick snack."
                )
            )
        }
    }
}