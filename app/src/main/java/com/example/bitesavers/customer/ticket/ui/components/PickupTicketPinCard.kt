package com.example.bitesavers.customer.ticket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun PickupTicketPinCard(pin: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF143B2A), // Dark Green
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.ticket_collection_pin),
                color = Color(0xFF1B5E20), // Darker green for text, or use Bright Green Color(0xFF00FF00) if preferred
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Loops through each character in the PIN string and creates a circle
                pin.forEach { digit ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF2E7D32), CircleShape), // Pin Circle Green
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit.toString(),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.ticket_pin_instruction),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PickupTicketPinCardPreview() {
    BiteSaversTheme {
        // Wrapped in a Box with padding just so it looks nice in the preview window
        Box(modifier = Modifier.padding(16.dp)) {
            PickupTicketPinCard(pin = "7667")
        }
    }
}