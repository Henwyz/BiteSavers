package com.example.bitesavers.customer.details.ui.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    val containerColor = if (isSafe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val contentColor = if (isSafe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError

    val iconResId = if (isSafe) R.drawable.ic_safe else R.drawable.ic_danger

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

            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = stringResource(id = R.string.cd_safety_status_icon),
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isSafe) stringResource(id = R.string.detail_safety_verified) else stringResource(id = R.string.detail_temperature_alert),
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = temperatureText.ifEmpty { stringResource(id = R.string.detail_live_temp_active) },
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
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