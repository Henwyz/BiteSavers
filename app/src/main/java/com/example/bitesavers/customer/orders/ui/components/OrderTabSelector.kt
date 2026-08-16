package com.example.bitesavers.customer.orders.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R
import com.example.bitesavers.customer.orders.data.OrderTab
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun OrderTabSelector(
    selectedTab: OrderTab,
    onTabSelected: (OrderTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Active Tab
            TabButton(
                text = stringResource(id = R.string.orders_tab_active),
                isSelected = selectedTab == OrderTab.ACTIVE,
                onClick = { onTabSelected(OrderTab.ACTIVE) },
                modifier = Modifier.weight(1f)
            )

            // History Tab
            TabButton(
                text = stringResource(id = R.string.orders_tab_history),
                isSelected = selectedTab == OrderTab.HISTORY,
                onClick = { onTabSelected(OrderTab.HISTORY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface.copy(alpha = 0f)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ================= Previews =================

@Preview(name = "History Selected", showBackground = true)
@Composable
private fun OrderTabSelectorHistoryPreview() {
    BiteSaversTheme {
        Surface {
            OrderTabSelector(
                selectedTab = OrderTab.HISTORY,
                onTabSelected = {}
            )
        }
    }
}

@Preview(name = "Active Selected", showBackground = true)
@Composable
private fun OrderTabSelectorActivePreview() {
    BiteSaversTheme {
        Surface {
            OrderTabSelector(
                selectedTab = OrderTab.ACTIVE,
                onTabSelected = {}
            )
        }
    }
}

@Preview(name = "Interactive Switcher", showBackground = true)
@Composable
private fun OrderTabSelectorInteractivePreview() {
    BiteSaversTheme {
        Surface {
            var selectedTab by remember { mutableStateOf(OrderTab.HISTORY) }
            OrderTabSelector(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    }
}