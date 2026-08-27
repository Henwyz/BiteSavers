package com.example.bitesavers.customer.details.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun FoodDetailTagsRow(
    distanceKm: Double,
    rating: Double = 4.8,
    weightKg: Double = 0.4,
    modifier: Modifier = Modifier
) {
    // Standard environmental formula: 1 kg of saved food prevents ~2.5 kg of greenhouse CO2 emission
    val co2SavedKg = (weightKg * 2.5).coerceAtLeast(0.5)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DetailTagChip(text = stringResource(id = R.string.detail_distance_away, distanceKm))
        DetailTagChip(text = "⭐ %.1f Rating".format(rating))
        DetailTagChip(text = "🌱 %.1f kg CO₂ saved".format(co2SavedKg))
    }
}

@Composable
private fun DetailTagChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodDetailTagsRowPreview() {
    BiteSaversTheme {
        FoodDetailTagsRow(
            distanceKm = 1.2,
            rating = 4.8,
            weightKg = 0.5
        )
    }
}