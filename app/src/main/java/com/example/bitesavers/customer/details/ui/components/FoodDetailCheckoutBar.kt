package com.example.bitesavers.customer.details.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun FoodDetailCheckoutBar(
    totalPrice: Double,
    isSoldOut: Boolean,
    isExpired: Boolean,
    onReserveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActionEnabled = !isSoldOut && !isExpired

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = onReserveClick,
                enabled = isActionEnabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            isSoldOut -> stringResource(id = R.string.btn_sold_out)
                            isExpired -> stringResource(id = R.string.btn_offer_expired)
                            else -> stringResource(id = R.string.action_reserve_now)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isActionEnabled) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                    if (isActionEnabled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.currency_rm, totalPrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Checkout Bar - Available")
@Composable
private fun FoodDetailCheckoutBarPreview() {
    BiteSaversTheme {
        FoodDetailCheckoutBar(
            totalPrice = 10.50,
            isSoldOut = false,
            isExpired = false,
            onReserveClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Checkout Bar - Sold Out")
@Composable
private fun FoodDetailCheckoutBarSoldOutPreview() {
    BiteSaversTheme {
        FoodDetailCheckoutBar(
            totalPrice = 10.50,
            isSoldOut = true,
            isExpired = false,
            onReserveClick = {}
        )
    }
}