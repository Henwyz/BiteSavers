package com.example.bitesavers.customer.details.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun FoodDetailStatusRow(
    hoursToClose: Int,
    stockLeft: Int,
    isOpen: Boolean = true,
    timeStatusText: String = "",
    modifier: Modifier = Modifier
) {
    val isSoldOut = stockLeft <= 0
    // Item is only truly expired if the store was open and current time passed the window
    val isExpired = isOpen && hoursToClose <= 0 && timeStatusText.equals("Closed", ignoreCase = true)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. TIME STATUS CARD (Opens In / Closes In / Free NGO Claim / Expired)
        Card(
            modifier = Modifier
                .weight(1f)
                .alpha(if (isSoldOut && !isExpired && isOpen) 0.6f else 1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    !isOpen -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notification),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = when {
                            isExpired -> MaterialTheme.colorScheme.error
                            !isOpen -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            !isOpen -> stringResource(id = R.string.badge_store_closed)
                            isExpired -> stringResource(id = R.string.badge_expired)
                            else -> stringResource(id = R.string.detail_expires_in_label)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            isExpired -> MaterialTheme.colorScheme.error
                            !isOpen -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                // Prioritizes the dynamic live minute string over static hours
                Text(
                    text = when {
                        timeStatusText.isNotBlank() -> timeStatusText.removePrefix("Closes in ")
                        isExpired -> stringResource(id = R.string.detail_status_expired)
                        hoursToClose > 0 -> stringResource(id = R.string.detail_expires_in_time, hoursToClose)
                        else -> stringResource(id = R.string.detail_status_expired)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = when {
                        isExpired -> MaterialTheme.colorScheme.error
                        !isOpen -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. STOCK LEFT CARD (Green tint / Error tint if sold out)
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSoldOut) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.detail_stock_left_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSoldOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isSoldOut) {
                        stringResource(id = R.string.stock_sold_out)
                    } else {
                        stockLeft.toString()
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isSoldOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(name = "Status Row - Available", showBackground = true)
@Composable
private fun FoodDetailStatusRowAvailablePreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailStatusRow(
                hoursToClose = 1,
                stockLeft = 6,
                isOpen = true,
                timeStatusText = "36m"
            )
        }
    }
}

@Preview(name = "Status Row - Opening Soon", showBackground = true)
@Composable
private fun FoodDetailStatusRowOpeningSoonPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailStatusRow(
                hoursToClose = 0,
                stockLeft = 6,
                isOpen = false,
                timeStatusText = "Opens in 6h"
            )
        }
    }
}

@Preview(name = "Status Row - Expired", showBackground = true)
@Composable
private fun FoodDetailStatusRowExpiredPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailStatusRow(
                hoursToClose = 0,
                stockLeft = 6,
                isOpen = true,
                timeStatusText = "Closed"
            )
        }
    }
}

@Preview(name = "Status Row - Sold Out", showBackground = true)
@Composable
private fun FoodDetailStatusRowSoldOutPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailStatusRow(
                hoursToClose = 2,
                stockLeft = 0,
                isOpen = true,
                timeStatusText = "Closes in 2h"
            )
        }
    }
}