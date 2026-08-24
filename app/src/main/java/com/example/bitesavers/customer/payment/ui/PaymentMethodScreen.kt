package com.example.bitesavers.customer.payment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.customer.payment.data.PaymentMethodsUiState
import com.example.bitesavers.customer.payment.data.SavedBankCard
import com.example.bitesavers.customer.payment.logic.PaymentMethodsViewModel
import com.example.bitesavers.customer.payment.ui.components.PaymentMethodCardItem
import com.example.bitesavers.customer.payment.ui.components.TopUpBottomSheet
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun PaymentMethodsRoute(
    viewModel: PaymentMethodsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PaymentMethodsScreen(
        state = uiState,
        onEvent = { event ->
            if (event is PaymentMethodsUiEvent.OnNavigateBack) {
                onNavigateBack()
            } else {
                viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    state: PaymentMethodsUiState,
    onEvent: (PaymentMethodsUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isAddingCard) "Add Debit / Credit Card" else stringResource(R.string.payment_methods_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isAddingCard) {
                            onEvent(PaymentMethodsUiEvent.OnToggleAddCard)
                        } else {
                            onEvent(PaymentMethodsUiEvent.OnNavigateBack)
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(id = R.string.cd_navigate_back)
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
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!state.isAddingCard) {
                // Section 1: In-App Balance Banner + Top Up Action
                Text(
                    text = "BiteSaver Balance",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "BiteSaver Pay",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Available: RM ${"%.2f".format(state.walletBalance)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Button(
                            onClick = { onEvent(PaymentMethodsUiEvent.OnShowTopUpSheet) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(text = "Top Up", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Section 2: Linked E-Wallets
                Text(
                    text = "Linked E-Wallets",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_payment),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Touch 'n Go eWallet",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (state.isTngLinked) state.tngPhone else "Not linked",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        TextButton(onClick = { onEvent(PaymentMethodsUiEvent.OnToggleTngLink) }) {
                            Text(
                                text = if (state.isTngLinked) "Unlink" else "Link",
                                color = if (state.isTngLinked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Section 3: Saved Bank Cards
                Text(
                    text = "Debit / Credit Cards",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                if (state.savedCards.isEmpty()) {
                    Text(
                        text = "No cards saved yet.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.savedCards.forEach { card ->
                        PaymentMethodCardItem(
                            card = card,
                            onSetDefault = { onEvent(PaymentMethodsUiEvent.OnSetDefaultCard(card.id)) },
                            onDelete = { onEvent(PaymentMethodsUiEvent.OnDeleteCard(card.id)) }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = { onEvent(PaymentMethodsUiEvent.OnToggleAddCard) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Add New Card", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Your payment credentials are encrypted & secure",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                // Section 4: Add New Card Form
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = state.cardNumber,
                            onValueChange = { onEvent(PaymentMethodsUiEvent.OnCardNumberChange(it)) },
                            label = { Text("Card Number") },
                            placeholder = { Text("16 digits (e.g. 4123 4567 8901 2345)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.cardHolder,
                            onValueChange = { onEvent(PaymentMethodsUiEvent.OnCardHolderChange(it)) },
                            label = { Text("Cardholder Name") },
                            placeholder = { Text("Name on card") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = state.expiryDate,
                                onValueChange = { onEvent(PaymentMethodsUiEvent.OnExpiryDateChange(it)) },
                                label = { Text("Expiry") },
                                placeholder = { Text("MM/YY") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = state.cvv,
                                onValueChange = { onEvent(PaymentMethodsUiEvent.OnCvvChange(it)) },
                                label = { Text("CVV") },
                                placeholder = { Text("3 digits") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = { onEvent(PaymentMethodsUiEvent.OnSaveCard) },
                            enabled = state.isFormValid,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save Card", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Top-Up Bottom Sheet Modal
        if (state.isTopUpSheetVisible) {
            TopUpBottomSheet(
                currentBalance = state.walletBalance,
                onConfirmTopUp = { amount ->
                    onEvent(PaymentMethodsUiEvent.OnConfirmTopUp(amount))
                },
                onDismiss = {
                    onEvent(PaymentMethodsUiEvent.OnDismissTopUpSheet)
                }
            )
        }
    }
}

@Preview(name = "Main Payment Methods Screen", showBackground = true)
@Composable
private fun PaymentMethodsScreenMainPreview() {
    BiteSaversTheme {
        PaymentMethodsScreen(
            state = PaymentMethodsUiState(
                walletBalance = 67.50,
                isTngLinked = true,
                tngPhone = "+60 12-*** 7890",
                savedCards = listOf(
                    SavedBankCard(
                        id = "1",
                        cardHolder = "Michelle Lim",
                        lastFourDigits = "4321",
                        expiryDate = "08/28",
                        isDefault = true
                    )
                ),
                isAddingCard = false
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Add New Card Mode", showBackground = true)
@Composable
private fun PaymentMethodsScreenAddCardPreview() {
    BiteSaversTheme {
        PaymentMethodsScreen(
            state = PaymentMethodsUiState(
                isAddingCard = true,
                cardNumber = "4123456789012345",
                cardHolder = "Michelle Lim",
                expiryDate = "08/28",
                cvv = "123"
            ),
            onEvent = {}
        )
    }
}