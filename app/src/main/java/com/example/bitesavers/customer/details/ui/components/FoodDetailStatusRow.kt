package com.example.bitesavers.customer.details.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    modifier: Modifier = Modifier
) {
    val isSoldOut = stockLeft <= 0
    val isExpired = hoursToClose <= 0

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. EXPIRES IN CARD (Yellow tint / Error tint if expired)
        Card(
            modifier = Modifier
                .weight(1f)
                .alpha(if (isSoldOut && !isExpired) 0.6f else 1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isExpired) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer // Soft yellow/orange background
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.detail_expires_in_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isExpired) {
                        stringResource(id = R.string.detail_status_expired)
                    } else {
                        stringResource(id = R.string.detail_expires_in_time, hoursToClose)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiaryContainer,
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
                    MaterialTheme.colorScheme.secondaryContainer // Soft green background
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

@Preview(showBackground = true, name = "Food Detail Status Row - Available")
@Composable
private fun FoodDetailStatusRowPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailStatusRow(
                hoursToClose = 1,
                stockLeft = 6
            )
        }
    }
}

@Preview(showBackground = true, name = "Food Detail Status Row - Sold Out")
@Composable
private fun FoodDetailStatusRowSoldOutPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailStatusRow(
                hoursToClose = 3,
                stockLeft = 0
            )
        }
    }
}