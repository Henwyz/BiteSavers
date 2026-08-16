package com.example.bitesavers.customer.orders.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R
import com.example.bitesavers.customer.orders.data.CustomerOrderItemUiModel
import com.example.bitesavers.customer.orders.data.OrderStatusType
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun OrderHistoryCard(
    order: CustomerOrderItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Row: Item Title & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.itemName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Store Name
            Text(
                text = order.storeName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Footer Row: Date, Short Order ID, and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clock),
                        contentDescription = stringResource(id = R.string.cd_orders_time_icon),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = order.formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = order.shortOrderId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = stringResource(id = R.string.orders_price_format, order.totalPrice),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ================= Previews =================

@Preview(name = "Completed Order Card", showBackground = true)
@Composable
private fun OrderHistoryCardCompletedPreview() {
    BiteSaversTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            OrderHistoryCard(
                order = CustomerOrderItemUiModel(
                    orderId = "1",
                    shortOrderId = "#BS-0701",
                    storeName = "Old Town Restaurant",
                    itemName = "Nasi Lemak Set",
                    formattedDate = "10 Jul 2026",
                    totalPrice = 4.50,
                    moneySaved = 4.50,
                    status = OrderStatusType.COMPLETED
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "Cancelled Order Card", showBackground = true)
@Composable
private fun OrderHistoryCardCancelledPreview() {
    BiteSaversTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            OrderHistoryCard(
                order = CustomerOrderItemUiModel(
                    orderId = "2",
                    shortOrderId = "#BS-0695",
                    storeName = "Penang Hawker Corner",
                    itemName = "Char Kway Teow",
                    formattedDate = "7 Jul 2026",
                    totalPrice = 3.50,
                    moneySaved = 3.50,
                    status = OrderStatusType.CANCELLED
                ),
                onClick = {}
            )
        }
    }
}