package com.example.bitesavers.customer.ticket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun PickupTicketSummaryCard(
    orderId: String,
    storeName: String,
    pickupWindow: String,
    itemName: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.ticket_summary_title),
                color = Color(0xFF143B2A),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.ticket_order_id, orderId),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            // Using specific drawables for each row
            SummaryRow(
                label = stringResource(id = R.string.ticket_label_restaurant),
                value = storeName,
                iconRes = R.drawable.ic_store // Add this to your res/drawable
            )
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            SummaryRow(
                label = stringResource(id = R.string.ticket_label_pickup_window),
                value = pickupWindow,
                iconRes = R.drawable.ic_clock // Add this to your res/drawable
            )
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            SummaryRow(
                label = stringResource(id = R.string.ticket_label_item),
                value = itemName,
                iconRes = R.drawable.ic_bag // Add this to your res/drawable
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, iconRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF4FAF5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Updated to painterResource!
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = Color(0xFF143B2A),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 10.sp, color = Color(0xFF757575), fontWeight = FontWeight.SemiBold)
            Text(text = value, fontSize = 14.sp, color = Color(0xFF143B2A), fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PickupTicketSummaryCardPreview() {
    BiteSaversTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFFF4FAF5))
                .padding(16.dp)
        ) {
            PickupTicketSummaryCard(
                orderId = "BS-28401",
                storeName = "Madam Lim Bakery",
                pickupWindow = "4:00 - 6:00 PM",
                itemName = "Butter Croissant"
            )
        }
    }
}