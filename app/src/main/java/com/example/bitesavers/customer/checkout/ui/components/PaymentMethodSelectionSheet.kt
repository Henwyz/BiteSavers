package com.example.bitesavers.customer.checkout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.data.model.PaymentMethod
import com.example.bitesavers.ui.theme.BiteSaversTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodSelectionSheet(
    selectedMethod: PaymentMethod,
    walletBalance: Double,
    onMethodSelect: (PaymentMethod) -> Unit,
    onAddNewPaymentClick: () -> Unit, // 👈 Added parameter
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // remember the state object controlling that bottom sheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // material 3 container that slides up from the bottom of the screen to present temporary actions
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        PaymentMethodSelectionContent(
            selectedMethod = selectedMethod,
            walletBalance = walletBalance,
            onMethodSelect = onMethodSelect,
            onAddNewPaymentClick = onAddNewPaymentClick // 👈 Passed down
        )
    }
}

@Composable
fun PaymentMethodSelectionContent(
    selectedMethod: PaymentMethod,
    walletBalance: Double,
    onMethodSelect: (PaymentMethod) -> Unit,
    onAddNewPaymentClick: () -> Unit, // 👈 Added parameter
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.payment_sheet_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        PaymentMethod.entries.forEach { method ->
            val isSelected = method == selectedMethod

            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }

            val borderColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }

            val icon: ImageVector = when (method) {
                PaymentMethod.BITESAVER_PAY -> Icons.Default.AccountBalanceWallet
                PaymentMethod.TNG_EWALLET -> Icons.Default.Payments
                PaymentMethod.BANK_CARD -> Icons.Default.CreditCard
                PaymentMethod.CASH_ON_PICKUP -> Icons.Default.Money
            }

            val titleText = when (method) {
                PaymentMethod.BITESAVER_PAY -> stringResource(R.string.payment_method_bitesaver_pay)
                PaymentMethod.TNG_EWALLET -> stringResource(R.string.payment_method_tng)
                PaymentMethod.BANK_CARD -> stringResource(R.string.payment_method_bank_card)
                PaymentMethod.CASH_ON_PICKUP -> stringResource(R.string.payment_method_cash_on_pickup)
            }

            val subtitleText = if (method == PaymentMethod.BITESAVER_PAY) {
                stringResource(R.string.checkout_balance_format, walletBalance)
            } else {
                method.subtitle
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(containerColor)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onMethodSelect(method) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                RadioButton(
                    selected = isSelected,
                    onClick = { onMethodSelect(method) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        TextButton(
            onClick = onAddNewPaymentClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.payment_sheet_manage_action),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentMethodSelectionSheetPreview() {
    BiteSaversTheme {
        Surface {
            PaymentMethodSelectionContent(
                selectedMethod = PaymentMethod.BITESAVER_PAY,
                walletBalance = 43.50,
                onMethodSelect = {},
                onAddNewPaymentClick = {}
            )
        }
    }
}