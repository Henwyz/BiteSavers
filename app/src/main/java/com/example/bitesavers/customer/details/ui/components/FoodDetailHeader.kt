package com.example.bitesavers.customer.details.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.ui.theme.BiteSaversTheme
import java.util.Locale

@Composable
fun FoodDetailHeader(
    offer: OfferUiModel,
    modifier: Modifier = Modifier
) {
    val savedAmount = offer.originalPrice - offer.currentPrice
    val categoryString = offer.category.name.lowercase().replaceFirstChar { it.titlecase(Locale.US) }

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
                text = stringResource(id = R.string.detail_category_format, categoryString),
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
                text = stringResource(id = R.string.original_price, offer.originalPrice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough
            )
            // Current discounted price
            Text(
                text = stringResource(id = R.string.currency_rm, offer.currentPrice),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            // Saved amount text
            Text(
                text = stringResource(id = R.string.detail_saved_amount, savedAmount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodDetailHeaderPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailHeader(
                offer = OfferUiModel(
                    id = "c1",
                    title = "Butter Croissant",
                    storeName = "Artisan Bakery",
                    imageResId = R.drawable.food_spaghetti,
                    discountPercent = 50,
                    currentPrice = 2.50,
                    originalPrice = 5.00,
                    distanceKm = 1.1,
                    quantityLeft = 6,
                    hoursToClose = 1,
                    category = DiscoveryCategory.BAKERY,
                    isEligibleForNgoFree = false,
                    liveTemperature = 22.0,
                    storageType = "COLD",
                    description = "Freshly baked butter croissants, perfect for a quick snack."
                )
            )
        }
    }
}