package com.example.bitesavers.customer.details.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun FoodDetailSafetyBanner(
    temperatureText: String,
    isSafe: Boolean,
    modifier: Modifier = Modifier
) {
    // Dynamic background and tint logic
    val containerColor = if (isSafe) Color(0xFF1B4D3E) else Color(0xFFC62828) // Deep green or warning red
    val contentColor = Color.White

    // Switch between your custom drawable resources depending on safety status
    // (Make sure to drop your custom icon files like ic_safety_check / ic_warning into res/drawable!)
    val iconResId = if (isSafe) R.drawable.ic_safe else R.drawable.ic_danger // Placeholder drawable references

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "Safety Status Icon",
                modifier = Modifier.size(32.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isSafe) "Smart Box Safety Verified" else "Temperature Alert!",
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = temperatureText.ifEmpty { "Live temp monitoring active" },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSafe) Color(0xFFC8E6C9) else Color(0xFFFFEBEE)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodDetailSafetyBannerPreview() {
    BiteSaversTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FoodDetailSafetyBanner(
                temperatureText = "Live temp: 60.0 °C - within safe hot storage zone",
                isSafe = true
            )
        }
    }
}