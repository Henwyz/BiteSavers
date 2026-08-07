package com.example.bitesavers.customer.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun FoodDetailStatusRow(
    hoursToClose: Int,
    stockLeft: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. EXPIRES IN CARD (Yellow tint)
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3CD) // Soft yellow background
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "⏰ EXPIRES IN",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF856404),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${hoursToClose}h 00m",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF856404),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. STOCK LEFT CARD (Green tint)
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFD4EDDA) // Soft green background
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🛒 STOCK LEFT",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF155724),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$stockLeft",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF155724),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodDetailStatusRowPreview() {
    BiteSaversTheme {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailStatusRow(
                hoursToClose = 1,
                stockLeft = 6
            )
        }
    }
}