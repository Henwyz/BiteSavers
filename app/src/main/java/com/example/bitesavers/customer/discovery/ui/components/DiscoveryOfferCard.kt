package com.example.bitesavers.customer.discovery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource // NEW IMPORT
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun DiscoveryOfferCard(
    offer: OfferUiModel,
    onClick: (OfferUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(offer) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Image(
                painter = painterResource(id = offer.imageResId),
                contentDescription = offer.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    // UPDATED: Uses the discount_tag string format
                    text = stringResource(id = R.string.discount_tag, offer.discountPercent),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = offer.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = offer.storeName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.size(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    // UPDATED: stringResource handles the double decimal formatting natively!
                    text = stringResource(id = R.string.currency_rm, offer.currentPrice),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    // UPDATED: stringResource handles the double decimal formatting natively!
                    text = stringResource(id = R.string.original_price, offer.originalPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                // UPDATED: Uses the new combined details format
                text = stringResource(
                    id = R.string.offer_details,
                    offer.distanceKm,
                    offer.quantityLeft,
                    offer.hoursToClose
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoveryOfferCardPreview() {
    BiteSaversTheme {
        DiscoveryOfferCard(
            offer = OfferUiModel(
                id = "1",
                title = "Bolognese Spaghetti",
                storeName = "Mr Lee Western Food",
                imageResId = R.drawable.food_spaghetti,
                discountPercent = 30,
                currentPrice = 10.50,
                originalPrice = 15.00,
                distanceKm = 1.9,
                quantityLeft = 10,
                hoursToClose = 2,
                category = DiscoveryCategory.HOT_MEALS, // UPDATED to match your new enum!
                isEligibleForNgoFree = false,
                liveTemperature = 65.0,
                storageType = "HOT",
                description = "Extra portions of our signature Bolognese Spaghetti."
            ),
            onClick = {}
        )
    }
}