package com.example.bitesavers.customer.sharedUI

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.bitesavers.R
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun OfferCard(
    offer: OfferUiModel,
    isSaved: Boolean = false,
    isNgoApproved: Boolean = false,
    onClick: (OfferUiModel) -> Unit,
    onToggleBookmark: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Condition: Approved NGO user + eligible item
    val isNgoFree = isNgoApproved && offer.isEligibleForNgoFree

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(offer.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = offer.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                error = {
                    Image(
                        painter = painterResource(id = R.drawable.food_spaghetti),
                        contentDescription = offer.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )

            // Shortened to Free Claim for NGO to avoid two lines
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isNgoFree) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.tertiary
                    )
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = if (isNgoFree) stringResource(id = R.string.badge_free_claim)
                    else stringResource(id = R.string.discount_tag, offer.discountPercent),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isNgoFree) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onTertiary,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { onToggleBookmark(offer.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(34.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_saved),
                    contentDescription = stringResource(
                        id = if (isSaved) R.string.cd_bookmark_saved else R.string.cd_bookmark_unsaved
                    ),
                    tint = if (isSaved) MaterialTheme.colorScheme.primary else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = offer.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 44.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Store Name row
            Text(
                text = offer.storeName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price Row using the mapper's unified visual price directly without double calculation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (isNgoFree) {
                    Text(
                        text = stringResource(id = R.string.price_free),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.currency_rm, offer.currentPrice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.currency_rm, offer.currentPrice),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.original_price, offer.originalPrice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Retains distance, quantity left, and closing text for discovery screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "• %.1f km  • %d left".format(offer.distanceKm, offer.quantityLeft),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "• %s".format(offer.timeStatusText.ifBlank { "Closes in ${offer.hoursToClose}h" }),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (!offer.isCurrentlyOpen) FontWeight.Medium else FontWeight.Normal,
                    color = if (!offer.isCurrentlyOpen) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
}

@Composable
fun CompactDiscoveryOfferCard(
    offer: OfferUiModel,
    isNgoApproved: Boolean = false,
    onClick: (OfferUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val isNgoFree = isNgoApproved && offer.isEligibleForNgoFree

    Card(
        modifier = modifier.clickable { onClick(offer) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(offer.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = offer.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        Image(
                            painter = painterResource(id = R.drawable.food_spaghetti),
                            contentDescription = offer.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )

                if (isNgoFree) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = stringResource(id = R.string.badge_free_claim),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f, fill = false),
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
                        text = "• %.1f km".format(offer.distanceKm),
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
private fun OfferCardConsumerPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OfferCard(
                offer = OfferUiModel(
                    id = "1",
                    title = "Bolognese Spaghetti with Signature Minced Beef Meatballs",
                    storeName = "Mr Lee Western Food",
                    imageResId = R.drawable.food_spaghetti,
                    imageUrl = null,
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
                isNgoApproved = false,
                onClick = {},
                onToggleBookmark = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Approved NGO View - Free Claim")
@Composable
private fun OfferCardNgoPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OfferCard(
                offer = OfferUiModel(
                    id = "2",
                    title = "Artisan Butter Croissant",
                    storeName = "Madam Lim Bakery",
                    imageResId = R.drawable.food_spaghetti,
                    imageUrl = null,
                    discountPercent = 50,
                    currentPrice = 5.00,
                    originalPrice = 10.00,
                    distanceKm = 0.8,
                    quantityLeft = 4,
                    hoursToClose = 1,
                    category = DiscoveryCategory.BAKERY,
                    isEligibleForNgoFree = true,
                    liveTemperature = 24.0,
                    storageType = "ROOM_TEMP",
                    description = "Freshly baked croissants nearing store closing."
                ),
                isSaved = true,
                isNgoApproved = true,
                onClick = {},
                onToggleBookmark = {}
            )
        }
    }
}