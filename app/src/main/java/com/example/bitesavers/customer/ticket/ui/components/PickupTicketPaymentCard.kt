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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun PickupTicketPaymentCard(totalPaid: Double) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.ticket_total_paid),
                    fontSize = 10.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = R.string.currency_rm, totalPaid),
                    fontSize = 24.sp,
                    color = Color(0xFF143B2A), // Dark Green
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(id = R.string.ticket_payment_method),
                    fontSize = 10.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .background(Color(0xFFC8E6C9), RoundedCornerShape(12.dp)) // Light green pill
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom payment icon
                    Icon(
                        painter = painterResource(id = R.drawable.ic_payment),
                        contentDescription = stringResource(id = R.string.cd_payment_method),
                        tint = Color(0xFF143B2A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.ticket_payment_bitesaver_pay),
                        fontSize = 12.sp,
                        color = Color(0xFF143B2A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PickupTicketPaymentCardPreview() {
    BiteSaversTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFFF4FAF5))
                .padding(16.dp)
        ) {
            PickupTicketPaymentCard(totalPaid = 5.00)
        }
    }
}