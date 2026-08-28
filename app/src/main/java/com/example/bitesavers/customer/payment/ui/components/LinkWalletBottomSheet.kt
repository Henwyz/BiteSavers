package com.example.bitesavers.customer.payment.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R
import com.example.bitesavers.customer.payment.logic.PaymentValidationUtils
import com.example.bitesavers.ui.theme.BiteSaversTheme

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
    val isPhoneValid = PaymentValidationUtils.isValidMalaysianPhone(phoneNumber)
    val isPhoneError = phoneNumber.isNotEmpty() && !isPhoneValid
    val isOtpValid = otp.trim().length == 6
    val isOtpError = otp.isNotEmpty() && !isOtpValid

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
                    isError = isPhoneError,
                    supportingText = {
                        if (isPhoneError) {
                            Text(
                                text = stringResource(R.string.error_invalid_phone),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onRequestOtp,
                    enabled = isPhoneValid,
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
                    isError = isOtpError,
                    supportingText = {
                        if (isOtpError) {
                            Text(
                                text = stringResource(R.string.error_invalid_otp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onConfirmLink,
                    enabled = isOtpValid,
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

@Preview(showBackground = true)
@Composable
private fun LinkWalletBottomSheetPreview() {
    BiteSaversTheme {
        LinkWalletBottomSheet(
            phoneNumber = "123456789",
            otp = "",
            isOtpStep = false,
            onPhoneChange = {},
            onOtpChange = {},
            onRequestOtp = {},
            onConfirmLink = {},
            onDismiss = {}
        )
    }
}