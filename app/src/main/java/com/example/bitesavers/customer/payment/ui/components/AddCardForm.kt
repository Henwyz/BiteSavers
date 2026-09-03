package com.example.bitesavers.customer.payment.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.customer.payment.logic.PaymentValidationUtils
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun AddCardForm(
    cardNumber: String,
    cardHolder: String,
    expiryDate: String,
    cvv: String,
    isFormValid: Boolean = false,
    onCardNumberChange: (String) -> Unit,
    onCardHolderChange: (String) -> Unit,
    onExpiryDateChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    onSaveCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCardNumberValid = PaymentValidationUtils.isValidCardNumber(cardNumber)
    val isCardNumberError = cardNumber.isNotEmpty() && !isCardNumberValid

    val isExpiryValid = PaymentValidationUtils.isValidExpiryDate(expiryDate)
    val isExpiryError = expiryDate.isNotEmpty() && !isExpiryValid

    val isCvvValid = PaymentValidationUtils.isValidCvv(cvv)
    val isCvvError = cvv.isNotEmpty() && !isCvvValid

    val isNameValid = cardHolder.trim().isNotBlank() && cardHolder.none { it.isDigit() }
    val canSubmit = isFormValid || (isCardNumberValid && isNameValid && isExpiryValid && isCvvValid)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { if (it.length <= 16) onCardNumberChange(it) },
                label = { Text(stringResource(R.string.field_card_number)) },
                placeholder = { Text(stringResource(R.string.hint_card_number)) },
                isError = isCardNumberError,
                supportingText = if (isCardNumberError) {
                    {
                        Text(
                            text = stringResource(R.string.error_invalid_card_number),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = cardHolder,
                onValueChange = { input ->
                    // Restricts digits so cardholder names only accept letters, spaces, hyphens, or apostrophes
                    val filtered = input.filter { it.isLetter() || it.isWhitespace() || it == '-' || it == '\'' }
                    onCardHolderChange(filtered)
                },
                label = { Text(stringResource(R.string.field_card_holder)) },
                placeholder = { Text(stringResource(R.string.hint_card_holder)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { if (it.length <= 4) onExpiryDateChange(it) },
                    label = { Text(stringResource(R.string.field_expiry_date)) },
                    placeholder = { Text(stringResource(R.string.hint_expiry_date)) },
                    visualTransformation = ExpiryDateVisualTransformation(),
                    isError = isExpiryError,
                    supportingText = if (isExpiryError) {
                        {
                            Text(
                                text = stringResource(R.string.error_invalid_expiry),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cvv,
                    onValueChange = { if (it.length <= 3) onCvvChange(it) },
                    label = { Text(stringResource(R.string.field_cvv)) },
                    placeholder = { Text(stringResource(R.string.hint_cvv)) },
                    isError = isCvvError,
                    supportingText = if (isCvvError) {
                        {
                            Text(
                                text = stringResource(R.string.error_invalid_cvv),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onSaveCard,
                enabled = canSubmit,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text(stringResource(R.string.action_save_card), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, name = "Add Card Form Preview")
@Composable
private fun AddCardFormPreview() {
    BiteSaversTheme {
        AddCardForm(
            cardNumber = "1234567812345678",
            cardHolder = "Sarah Tan",
            expiryDate = "1228",
            cvv = "123",
            isFormValid = true,
            onCardNumberChange = {},
            onCardHolderChange = {},
            onExpiryDateChange = {},
            onCvvChange = {},
            onSaveCard = {}
        )
    }
}