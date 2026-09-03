package com.example.bitesavers.customer.checkout.ui.components

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.data.model.PaymentMethod
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun CheckoutPaymentCard(
    paymentMethod: PaymentMethod,
    walletBalance: Double,
    isTngLinked: Boolean = false,
    tngPhone: String = "",
    savedCardDigits: String? = null,
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleText = when (paymentMethod) {
        PaymentMethod.BITESAVER_PAY -> stringResource(R.string.payment_method_bitesaver_pay)
        PaymentMethod.TNG_EWALLET -> stringResource(R.string.payment_method_tng)
        PaymentMethod.BANK_CARD -> stringResource(R.string.payment_method_bank_card)
        PaymentMethod.CASH_ON_PICKUP -> stringResource(R.string.payment_method_cash_on_pickup)
    }

    val subtitleText = when (paymentMethod) {
        PaymentMethod.BITESAVER_PAY -> {
            stringResource(R.string.checkout_balance_format, walletBalance)
        }
        PaymentMethod.TNG_EWALLET -> {
            if (isTngLinked && tngPhone.isNotBlank()) {
                tngPhone
            } else {
                stringResource(R.string.payment_not_linked)
            }
        }
        PaymentMethod.BANK_CARD -> {
            if (!savedCardDigits.isNullOrBlank()) {
                stringResource(R.string.payment_card_format, savedCardDigits)
            } else {
                stringResource(R.string.payment_no_cards)
            }
        }
        PaymentMethod.CASH_ON_PICKUP -> {
            stringResource(R.string.payment_method_cash_subtitle)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.checkout_payment_method_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                TextButton(onClick = onChangeClick) {
                    Text(
                        text = stringResource(R.string.checkout_action_change),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_payment),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
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
            }
        }
    }
}

@Preview(name = "Checkout Payment Card - Light", showBackground = true)
@Preview(name = "Checkout Payment Card - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun CheckoutPaymentCardPreview() {
    BiteSaversTheme {
        CheckoutPaymentCard(
            paymentMethod = PaymentMethod.TNG_EWALLET,
            walletBalance = 77.50,
            isTngLinked = true,
            tngPhone = "+60 19-*** 9592",
            onChangeClick = {}
        )
    }
}