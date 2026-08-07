package com.example.bitesavers.customer.details.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun FoodDetailTagsRow(
    distanceKm: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()), // This allows the tags to swipe sideways!
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DetailTagChip(text = "📍 ${distanceKm} km away")
        DetailTagChip(text = "⭐ 4.8 (120+ ratings)")
        DetailTagChip(text = "🌱 ~1.2kg CO₂ saved")
    }
}

@Composable
private fun DetailTagChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF1F3F4)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodDetailTagsRowPreview() {
    BiteSaversTheme {
        FoodDetailTagsRow(distanceKm = 1.1)
    }
}