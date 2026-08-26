package com.example.bitesavers.customer.saved.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.data.model.OfferUiModel

@Composable
fun SavedOfferCard(
    offer: OfferUiModel,
    onOfferClick: (String) -> Unit,
    onRemoveClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSoldOut = offer.quantityLeft <= 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isSoldOut) { onOfferClick(offer.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Thumbnail with Sold Out Overlay
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = offer.imageResId),
                    contentDescription = offer.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isSoldOut) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = stringResource(R.string.badge_sold_out),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = offer.storeName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSoldOut) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.currency_rm, offer.currentPrice),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSoldOut) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                    )

                    if (offer.originalPrice > offer.currentPrice) {
                        Text(
                            text = stringResource(R.string.original_price, offer.originalPrice),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                Text(
                    text = if (isSoldOut) {
                        stringResource(R.string.badge_sold_out)
                    } else {
                        stringResource(R.string.remaining_count, offer.quantityLeft)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSoldOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            // Remove/Bookmark toggle button
            IconButton(
                onClick = { onRemoveClick(offer.id) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your bookmark/trash icon drawable
                    contentDescription = stringResource(R.string.action_remove_saved),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}