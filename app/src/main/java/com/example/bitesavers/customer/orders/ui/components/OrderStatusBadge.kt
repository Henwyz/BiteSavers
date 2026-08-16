package com.example.bitesavers.customer.orders.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R
import com.example.bitesavers.customer.orders.data.OrderStatusType
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun OrderStatusBadge(
    status: OrderStatusType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, textRes) = when (status) {
        OrderStatusType.COMPLETED -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            R.string.orders_status_completed
        )
        OrderStatusType.CANCELLED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            R.string.orders_status_cancelled
        )
        OrderStatusType.READY_FOR_PICKUP -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            R.string.orders_status_ready
        )
    }

    Text(
        text = stringResource(id = textRes),
        color = textColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

// ================= Previews =================

@Preview(name = "All Status Badges", showBackground = true)
@Composable
private fun OrderStatusBadgePreview() {
    BiteSaversTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrderStatusBadge(status = OrderStatusType.COMPLETED)
                OrderStatusBadge(status = OrderStatusType.CANCELLED)
                OrderStatusBadge(status = OrderStatusType.READY_FOR_PICKUP)
            }
        }
    }
}