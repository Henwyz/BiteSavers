package com.example.bitesavers.customer.saved.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun SavedOfferCard(
    offer: OfferUiModel,
    onOfferClick: (String) -> Unit,
    onRemoveClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSoldOut = offer.quantityLeft <= 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOfferClick(offer.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food image thumbnail with network loading and fallback drawable
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(offer.imageUrl)
                    .crossfade(true)
                    .error(offer.imageResId)
                    .placeholder(offer.imageResId)
                    .build(),
                contentDescription = stringResource(R.string.cd_food_item_image),
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Center details: store name, title, pricing, and clean stock remaining status
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Store name only (Rating removed)
                Text(
                    text = offer.storeName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.currency_rm, offer.currentPrice),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.original_price, offer.originalPrice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                // Displays "Sold Out" when stock is 0, otherwise shows only remaining count (Distance removed)
                if (isSoldOut) {
                    Text(
                        text = stringResource(R.string.stock_sold_out),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = stringResource(R.string.stock_remaining_count, offer.quantityLeft),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Remove bookmark action button: uses filled bookmark/heart icon
            IconButton(
                onClick = { onRemoveClick(offer.id) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_saved_filled),
                    contentDescription = stringResource(R.string.cd_bookmark_saved),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Previews the card in an active/available state
@Preview(showBackground = true, name = "Saved Card - Available")
@Composable
private fun SavedOfferCardAvailablePreview() {
    BiteSaversTheme {
        SavedOfferCard(
            offer = OfferUiModel(
                id = "e1",
                title = "Japanese Matcha Mille Crepe",
                storeName = "Sweet Treats Cafe",
                imageResId = R.drawable.food_spaghetti,
                discountPercent = 50,
                currentPrice = 8.00,
                originalPrice = 16.00,
                distanceKm = 0.0,
                quantityLeft = 3,
                hoursToClose = 2,
                category = DiscoveryCategory.DESSERTS,
                isEligibleForNgoFree = false,
                liveTemperature = 4.0,
                storageType = "COLD",
                description = "Delicate layers of crepe with matcha cream."
            ),
            onOfferClick = {},
            onRemoveClick = {}
        )
    }
}

// Previews the card when inventory reaches 0 (Sold Out state)
@Preview(showBackground = true, name = "Saved Card - Sold Out")
@Composable
private fun SavedOfferCardSoldOutPreview() {
    BiteSaversTheme {
        SavedOfferCard(
            offer = OfferUiModel(
                id = "e2",
                title = "Japanese Matcha Mille Crepe",
                storeName = "Sweet Treats Cafe",
                imageResId = R.drawable.food_spaghetti,
                discountPercent = 50,
                currentPrice = 8.00,
                originalPrice = 16.00,
                distanceKm = 0.0,
                quantityLeft = 0,
                hoursToClose = 2,
                category = DiscoveryCategory.DESSERTS,
                isEligibleForNgoFree = false,
                liveTemperature = 4.0,
                storageType = "COLD",
                description = "Delicate layers of crepe with matcha cream."
            ),
            onOfferClick = {},
            onRemoveClick = {}
        )
    }
}