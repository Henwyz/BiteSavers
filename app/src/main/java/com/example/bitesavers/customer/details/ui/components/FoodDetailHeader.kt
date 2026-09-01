package com.example.bitesavers.customer.details.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.getDisplayNameRes
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun FoodDetailHeader(
    offer: OfferUiModel,
    onStoreClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val savedAmount = offer.originalPrice - offer.currentPrice
    // Resolves localized label directly from XML resources (e.g., "Hot Meals", "Bakery")
    val categoryString = stringResource(id = offer.category.getDisplayNameRes())

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
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

        // Store Navigation Row: Displays restaurant name with an icon, clickable to open Store Detail
        Row(
            modifier = Modifier
                .clickable { onStoreClick() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = offer.storeName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Food Detail Header - Hot Meals")
@Composable
private fun FoodDetailHeaderHotMealsPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailHeader(
                offer = OfferUiModel(
                    id = "e3",
                    storeId = "store_1",
                    title = "Chicken Bolognese Pasta",
                    storeName = "Apollo Western & Pasta",
                    imageResId = R.drawable.food_spaghetti,
                    discountPercent = 45,
                    currentPrice = 9.90,
                    originalPrice = 18.00,
                    distanceKm = 0.3,
                    quantityLeft = 6,
                    hoursToClose = 15,
                    category = DiscoveryCategory.HOT_MEALS,
                    isEligibleForNgoFree = true,
                    liveTemperature = 25.0,
                    storageType = "ROOM_TEMP",
                    description = "Hearty minced chicken pasta in slow-simmered tomato sauce."
                ),
                onStoreClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Food Detail Header - Bakery")
@Composable
private fun FoodDetailHeaderPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailHeader(
                offer = OfferUiModel(
                    id = "c1",
                    storeId = "store_2",
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
                ),
                onStoreClick = {}
            )
        }
    }
}