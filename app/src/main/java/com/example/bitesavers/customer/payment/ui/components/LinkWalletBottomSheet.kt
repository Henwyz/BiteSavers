package com.example.bitesavers.customer.payment.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkWalletBottomSheet(
    phoneNumber: String,
    otp: String,
    isOtpStep: Boolean,
    onPhoneChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onRequestOtp: () -> Unit,
    onConfirmLink: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (!isOtpStep) stringResource(R.string.link_wallet_sheet_title) else stringResource(R.string.link_wallet_verify_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (!isOtpStep) {
                    stringResource(R.string.link_wallet_phone_desc)
                } else {
                    stringResource(R.string.link_wallet_otp_desc, phoneNumber)
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isOtpStep) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneChange,
                    label = { Text(stringResource(R.string.link_wallet_mobile_label)) },
                    placeholder = { Text(stringResource(R.string.link_wallet_mobile_placeholder)) },
                    prefix = { Text("+60 ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onRequestOtp,
                    enabled = phoneNumber.trim().length >= 8,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.action_get_otp), fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) onOtpChange(it) },
                    label = { Text(stringResource(R.string.link_wallet_otp_label)) },
                    placeholder = { Text(stringResource(R.string.link_wallet_otp_placeholder)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onConfirmLink,
                    enabled = otp.trim().length == 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.action_confirm_link_account), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}