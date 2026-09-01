package com.example.bitesavers.customer.payment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.customer.payment.data.PaymentMethodsUiState
import com.example.bitesavers.customer.payment.data.SavedBankCard
import com.example.bitesavers.customer.payment.logic.PaymentMethodsViewModel
import com.example.bitesavers.customer.payment.ui.components.AddCardForm
import com.example.bitesavers.customer.payment.ui.components.LinkWalletBottomSheet
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
                        text = if (state.isAddingCard) stringResource(R.string.payment_title_add_card) else stringResource(R.string.payment_methods_title),
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
                ),
                // Removes excessive system bar inset padding to eliminate the large gap below the status bar
                windowInsets = WindowInsets(0.dp)
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
                    text = stringResource(R.string.payment_section_bitesaver_balance),
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
                                    painter = painterResource(id = R.drawable.ic_payment),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.payment_bitesaver_pay),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.payment_available_balance, state.walletBalance),
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
                            Text(text = stringResource(R.string.action_top_up), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Section 2: Linked E-Wallets
                Text(
                    text = stringResource(R.string.payment_section_linked_ewallets),
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
                                    text = stringResource(R.string.payment_tng_title),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (state.isTngLinked) state.tngPhone else stringResource(R.string.payment_not_linked),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        TextButton(onClick = {
                            if (state.isTngLinked) {
                                onEvent(PaymentMethodsUiEvent.OnUnlinkWallet)
                            } else {
                                onEvent(PaymentMethodsUiEvent.OnShowLinkWalletSheet)
                            }
                        }) {
                            Text(
                                text = if (state.isTngLinked) stringResource(R.string.action_unlink) else stringResource(R.string.action_link),
                                color = if (state.isTngLinked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Section 3: Saved Bank Cards
                Text(
                    text = stringResource(R.string.payment_section_cards),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                if (state.savedCards.isEmpty()) {
                    Text(
                        text = stringResource(R.string.payment_no_cards),
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
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.action_add_new_card), fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.payment_security_notice),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                // Section 4: Add Card Form Component
                AddCardForm(
                    cardNumber = state.cardNumber,
                    cardHolder = state.cardHolder,
                    expiryDate = state.expiryDate,
                    cvv = state.cvv,
                    isFormValid = state.isFormValid,
                    onCardNumberChange = { onEvent(PaymentMethodsUiEvent.OnCardNumberChange(it)) },
                    onCardHolderChange = { onEvent(PaymentMethodsUiEvent.OnCardHolderChange(it)) },
                    onExpiryDateChange = { onEvent(PaymentMethodsUiEvent.OnExpiryDateChange(it)) },
                    onCvvChange = { onEvent(PaymentMethodsUiEvent.OnCvvChange(it)) },
                    onSaveCard = { onEvent(PaymentMethodsUiEvent.OnSaveCard) }
                )
            }
        }

        // Top-Up Bottom Sheet Modal
        if (state.isTopUpSheetVisible) {
            TopUpBottomSheet(
                currentBalance = state.walletBalance,
                isProcessing = state.isProcessingPayment,
                onConfirmTopUp = { amount, source ->
                    onEvent(PaymentMethodsUiEvent.OnConfirmTopUp(amount, source))
                },
                onDismiss = {
                    onEvent(PaymentMethodsUiEvent.OnDismissTopUpSheet)
                }
            )
        }

        // Link E-Wallet Phone & OTP Sheet Modal
        if (state.isLinkWalletSheetVisible) {
            LinkWalletBottomSheet(
                phoneNumber = state.linkWalletPhone,
                otp = state.linkWalletOtp,
                isOtpStep = state.isOtpStep,
                onPhoneChange = { onEvent(PaymentMethodsUiEvent.OnLinkPhoneChange(it)) },
                onOtpChange = { onEvent(PaymentMethodsUiEvent.OnLinkOtpChange(it)) },
                onRequestOtp = { onEvent(PaymentMethodsUiEvent.OnRequestOtp) },
                onConfirmLink = { onEvent(PaymentMethodsUiEvent.OnConfirmLinkWallet) },
                onDismiss = { onEvent(PaymentMethodsUiEvent.OnDismissLinkWalletSheet) }
            )
        }
    }
}

// Previews the main payment methods screen with saved cards
@Preview(showBackground = true, name = "Payment Methods - Populated")
@Composable
private fun PaymentMethodsScreenPopulatedPreview() {
    BiteSaversTheme {
        PaymentMethodsScreen(
            state = PaymentMethodsUiState(
                walletBalance = 29.50,
                isTngLinked = false,
                tngPhone = "",
                savedCards = listOf(
                    SavedBankCard(
                        id = "1",
                        cardHolder = "Sarah Tan",
                        lastFourDigits = "4321",
                        expiryDate = "08/28",
                        isDefault = true
                    )
                )
            ),
            onEvent = {}
        )
    }
}

// Previews the screen in Add Card mode
@Preview(showBackground = true, name = "Payment Methods - Add Card Mode")
@Composable
private fun PaymentMethodsScreenAddCardPreview() {
    BiteSaversTheme {
        PaymentMethodsScreen(
            state = PaymentMethodsUiState(
                isAddingCard = true,
                cardNumber = "4111222233334444",
                cardHolder = "Sarah Tan",
                expiryDate = "1228",
                cvv = "123",
                isFormValid = true
            ),
            onEvent = {}
        )
    }
}