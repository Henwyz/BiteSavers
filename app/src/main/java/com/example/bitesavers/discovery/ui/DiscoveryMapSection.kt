package com.example.bitesavers.discovery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.discovery.data.NearbyDealMarkerUiModel
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun DiscoveryMapSection(
    markers: List<NearbyDealMarkerUiModel>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = "Nearby deals map",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //Takes the first 4 to display in the map
            markers.take(4).forEach { marker ->
                MarkerBubble(label = marker.labelPrice)
            }
        }
    }
}

@Composable
private fun MarkerBubble(label: String) {
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 34.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoveryMapSectionPreview() {
    BiteSaversTheme {
        DiscoveryMapSection(
            markers = listOf(
                NearbyDealMarkerUiModel("1", "RM 9", 0.0, 0.0),
                NearbyDealMarkerUiModel("2", "RM 7", 0.0, 0.0),
                NearbyDealMarkerUiModel("3", "RM 12", 0.0, 0.0)
            )
        )
    }
}