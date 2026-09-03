package com.example.bitesavers.business.wallet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R
import com.example.bitesavers.business.payment.logic.MerchantWalletViewModel
import com.example.bitesavers.data.remote.dto.MerchantPayoutDto

// Screen for displaying store balance, requesting withdrawals, and viewing payout history
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantWalletScreen(
    viewModel: MerchantWalletViewModel,
    storeId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("") }
    var cardInput by remember { mutableStateOf("") }

    // Additional state fields for expiry and CVV validation
    var expiryInput by remember { mutableStateOf("") }
    var cvvInput by remember { mutableStateOf("") }

    // Loads live store wallet data and history when screen opens
    LaunchedEffect(storeId) {
        viewModel.loadWalletData(storeId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.merchant_wallet_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // Uses painterResource for icons as required
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Card highlighting available funds with a primary withdraw action button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.merchant_available_balance),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "RM %.2f".format(viewModel.balance),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = { showWithdrawDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.merchant_withdraw_button))
                    }
                }
            }

            // Displays success feedback message on the main screen if needed
            viewModel.successMessage?.let { success ->
                Text(
                    text = success,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Section header for payout history records
            Text(
                text = stringResource(R.string.merchant_payout_history),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Recent payout history list displaying transaction entries and status pill badges
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.payoutHistory) { payout ->
                    PayoutHistoryItem(payout = payout)
                }
            }
        }
    }

    // Payout input dialog with strict validation for amount, 16-digit card, MM/YY expiry, and 3-digit CVV
    if (showWithdrawDialog) {
        // Capture string resources inside the Composable scope for localization safety
        val errCardLength = stringResource(R.string.error_card_length)
        val errExpiryInvalid = stringResource(R.string.error_expiry_invalid)
        val errMonthRange = stringResource(R.string.error_month_range)
        val errYearRange = stringResource(R.string.error_year_range)
        val errCvvLength = stringResource(R.string.error_cvv_length)

        AlertDialog(
            onDismissRequest = {
                showWithdrawDialog = false
                viewModel.clearMessages()
            },
            title = { Text(stringResource(R.string.dialog_withdraw_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Error message displayed directly inside the dialog above inputs
                    viewModel.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // 1. Amount Field (Digits & decimals only)
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' }) {
                                amountInput = input
                            }
                        },
                        label = { Text(stringResource(R.string.dialog_amount_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // 2. Card Number Field (Strictly numbers only, max 16 digits)
                    OutlinedTextField(
                        value = cardInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 16) {
                                cardInput = input
                            }
                        },
                        label = { Text(stringResource(R.string.dialog_card_number_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Row for Expiry Date and CVV inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 3. Expiry Date Field (MMYY format, digits only, max 4 characters)
                        OutlinedTextField(
                            value = expiryInput,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }.take(4)
                                if (digitsOnly.length <= 4) {
                                    expiryInput = digitsOnly
                                }
                            },
                            label = { Text(stringResource(R.string.dialog_exp_hint)) },
                            placeholder = { Text("MMYY") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // 4. CVV Field (Strictly numbers only, max 3 digits)
                        OutlinedTextField(
                            value = cvvInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 3) {
                                    cvvInput = input
                                }
                            },
                            label = { Text(stringResource(R.string.dialog_cvv_hint)) },
                            placeholder = { Text("123") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = amountInput.toDoubleOrNull() ?: 0.0

                        // Validations for Card, Expiry, and CVV constraints using localized strings
                        when {
                            cardInput.length != 16 -> {
                                viewModel.errorMessage = errCardLength
                            }
                            expiryInput.length < 4 -> {
                                viewModel.errorMessage = errExpiryInvalid
                            }
                            else -> {
                                val month = expiryInput.take(2).toIntOrNull() ?: 0
                                val year = expiryInput.takeLast(2).toIntOrNull() ?: 0

                                when {
                                    month !in 1..12 -> {
                                        viewModel.errorMessage = errMonthRange
                                    }
                                    year < 27 -> {
                                        viewModel.errorMessage = errYearRange
                                    }
                                    cvvInput.length != 3 -> {
                                        viewModel.errorMessage = errCvvLength
                                    }
                                    else -> {
                                        // Clear errors and run request withdrawal
                                        viewModel.requestWithdrawal(storeId, amount, cardInput, expiryInput, cvvInput) {
                                            showWithdrawDialog = false
                                            amountInput = ""
                                            cardInput = ""
                                            expiryInput = ""
                                            cvvInput = ""
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.merchant_withdraw_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showWithdrawDialog = false
                        expiryInput = ""
                        cvvInput = ""
                        viewModel.clearMessages()
                    }
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

// Composable row item displaying individual payout transaction details and status badges
@Composable
fun PayoutHistoryItem(payout: MerchantPayoutDto) {
    val statusColor = when (payout.status.uppercase()) {
        "COMPLETED" -> MaterialTheme.colorScheme.primary
        "REJECTED" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Card: **** ${payout.cardNumber.takeLast(4)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = payout.createdAt ?: "Recent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "RM %.2f".format(payout.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = payout.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
        }
    }
}