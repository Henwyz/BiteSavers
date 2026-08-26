package com.example.bitesavers.customer.discovery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun DiscoveryOfferCard(
    offer: OfferUiModel,
    isSaved: Boolean = false,
    userRole: UserRole = UserRole.CONSUMER, // Accepts current role
    onClick: (OfferUiModel) -> Unit,
    onToggleBookmark: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Condition: NGO user + eligible item + closing within 1 hour
    val isNgoFree = userRole == UserRole.NGO && offer.isEligibleForNgoFree && offer.hoursToClose <= 1

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

            // Badge Display: Shows FREE CLAIM for NGOs, or standard Discount % for Consumers
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isNgoFree) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.tertiary
                    )
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = if (isNgoFree) {
                        stringResource(id = R.string.badge_ngo_free)
                    } else {
                        stringResource(id = R.string.discount_tag, offer.discountPercent)
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isNgoFree) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            // Bookmark / Favourite Toggle Button
            IconButton(
                onClick = { onToggleBookmark(offer.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isSaved) R.drawable.ic_launcher_foreground else R.drawable.ic_launcher_foreground // Replace with your filled/outline bookmark drawables
                    ),
                    contentDescription = stringResource(id = R.string.cd_bookmark_icon),
                    tint = if (isSaved) MaterialTheme.colorScheme.primary else Color.White,
                    modifier = Modifier.size(18.dp)
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

            // Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isNgoFree) {
                    Text(
                        text = stringResource(id = R.string.price_free),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.currency_rm, offer.currentPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.currency_rm, offer.currentPrice),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.original_price, offer.originalPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.size(8.dp))

            Text(
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

//Offer card for map pin
@Composable
fun CompactDiscoveryOfferCard(
    offer: OfferUiModel,
    userRole: UserRole = UserRole.CONSUMER,
    onClick: (OfferUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val isNgoFree = userRole == UserRole.NGO && offer.isEligibleForNgoFree && offer.hoursToClose <= 1

    Card(
        modifier = modifier
            .clickable { onClick(offer) }, // REMOVED fillMaxWidth() here!
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Thumbnail
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = offer.imageResId),
                    contentDescription = offer.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isNgoFree) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "FREE",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details Column
            Column(
                modifier = Modifier.weight(1f, fill = false), // Allows flexible sizing without stretching
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = offer.storeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isNgoFree) stringResource(id = R.string.price_free)
                        else stringResource(id = R.string.currency_rm, offer.currentPrice),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "• ${offer.distanceKm} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Consumer View - Standard Price")
@Composable
private fun DiscoveryOfferCardConsumerPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
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
                    category = DiscoveryCategory.HOT_MEALS,
                    isEligibleForNgoFree = false,
                    liveTemperature = 65.0,
                    storageType = "HOT",
                    description = "Extra portions of our signature Bolognese Spaghetti."
                ),
                isSaved = false,
                userRole = UserRole.CONSUMER,
                onClick = {},
                onToggleBookmark = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "NGO View - Free Claim")
@Composable
private fun DiscoveryOfferCardNgoPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DiscoveryOfferCard(
                offer = OfferUiModel(
                    id = "2",
                    title = "Butter Croissant",
                    storeName = "Madam Lim Bakery",
                    imageResId = R.drawable.food_spaghetti, // Swap with your bakery image resource
                    discountPercent = 50,
                    currentPrice = 5.00,
                    originalPrice = 10.00,
                    distanceKm = 0.8,
                    quantityLeft = 4,
                    hoursToClose = 1, // <= 1 hour remaining triggers NGO Free Claim
                    category = DiscoveryCategory.BAKERY,
                    isEligibleForNgoFree = true,
                    liveTemperature = 24.0,
                    storageType = "ROOM_TEMP",
                    description = "Freshly baked croissants nearing store closing."
                ),
                isSaved = true,
                userRole = UserRole.NGO,
                onClick = {},
                onToggleBookmark = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Compact Card - Consumer View")
@Composable
private fun CompactDiscoveryOfferCardConsumerPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(12.dp)) {
            CompactDiscoveryOfferCard(
                offer = OfferUiModel(
                    id = "o1",
                    title = "Bolognese Spaghetti",
                    storeName = "Mr Lee Western Food",
                    imageResId = R.drawable.food_spaghetti,
                    discountPercent = 30,
                    currentPrice = 10.50,
                    originalPrice = 15.00,
                    distanceKm = 1.9,
                    quantityLeft = 10,
                    hoursToClose = 2,
                    category = DiscoveryCategory.HOT_MEALS,
                    isEligibleForNgoFree = false,
                    liveTemperature = 65.0,
                    storageType = "HOT",
                    description = "Signature spaghetti with minced beef."
                ),
                userRole = UserRole.CONSUMER,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Compact Card - NGO View")
@Composable
private fun CompactDiscoveryOfferCardNgoPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(12.dp)) {
            CompactDiscoveryOfferCard(
                offer = OfferUiModel(
                    id = "o2",
                    title = "Nasi Goreng Ayam",
                    storeName = "Nani Kafe",
                    imageResId = R.drawable.food_nasi_goreng,
                    discountPercent = 40,
                    currentPrice = 9.00,
                    originalPrice = 15.00,
                    distanceKm = 3.2,
                    quantityLeft = 6,
                    hoursToClose = 1, // <= 1 hour triggers NGO Free Claim
                    category = DiscoveryCategory.HOT_MEALS,
                    isEligibleForNgoFree = true,
                    liveTemperature = 62.0,
                    storageType = "HOT",
                    description = "Freshly made fried rice."
                ),
                userRole = UserRole.NGO,
                onClick = {}
            )
        }
    }
}