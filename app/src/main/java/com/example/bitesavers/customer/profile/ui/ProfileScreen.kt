package com.example.bitesavers.customer.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.customer.profile.data.NgoStatus
import com.example.bitesavers.customer.profile.logic.ProfileViewModel
import com.example.bitesavers.customer.profile.logic.SustainabilityCalculator

@Composable
fun ProfileScreen(
    onRegisterAsNgoClick: () -> Unit,
    onViewNgoDetailsClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit, // <--- ADDED: Navigation callback for Payment Methods screen
    onSignOutClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val impact = SustainabilityCalculator.calculateImpact(profile.mealsRescued)
    val menuIconTint = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ---------- Header ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onSignOutClick,
                    label = {
                        Text(
                            text = stringResource(R.string.profile_sign_out),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_danger),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    border = null
                )
                IconButton(
                    onClick = { /* TODO wire to notifications */ },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = stringResource(R.string.cd_notifications),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        profile.avatarInitials,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    profile.name,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    profile.email,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    profile.memberSinceLabel,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }

        // ---------- Wallet card ----------
        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            stringResource(R.string.profile_wallet_label),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stringResource(R.string.currency_rm, profile.walletBalance),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Button(
                    onClick = { /* TODO wire to top-up flow */ },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(stringResource(R.string.profile_top_up))
                }
            }
        }

        // ---------- Menu list ----------
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp)
        ) {
            ProfileMenuRow(
                icon = { Icon(Icons.Filled.VolunteerActivism, contentDescription = null, tint = menuIconTint, modifier = Modifier.size(16.dp)) },
                labelResId = when (profile.ngoStatus) {
                    NgoStatus.NONE -> R.string.profile_register_ngo
                    NgoStatus.PENDING -> R.string.profile_ngo_pending
                    NgoStatus.APPROVED -> R.string.profile_ngo_approved
                },
                onClick = {
                    // Approved NGOs land on the read-only details screen (with
                    // an Edit FAB); everyone else goes straight to the
                    // registration form.
                    if (profile.ngoStatus == NgoStatus.APPROVED) {
                        onViewNgoDetailsClick()
                    } else {
                        onRegisterAsNgoClick()
                    }
                }
            )
            ProfileMenuRow(
                icon = { Icon(Icons.Filled.CreditCard, contentDescription = null, tint = menuIconTint, modifier = Modifier.size(16.dp)) },
                labelResId = R.string.profile_payment_methods,
                onClick = onPaymentMethodsClick // <--- MODIFIED: Trigger payment methods navigation
            )
            ProfileMenuRow(
                icon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = menuIconTint, modifier = Modifier.size(16.dp)) },
                labelResId = R.string.profile_privacy_security,
                onClick = {}
            )
            ProfileMenuRow(
                icon = { Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null, tint = menuIconTint, modifier = Modifier.size(16.dp)) },
                labelResId = R.string.profile_help_support,
                onClick = {}
            )
            ProfileMenuRow(
                icon = { Icon(Icons.Filled.Eco, contentDescription = null, tint = menuIconTint, modifier = Modifier.size(16.dp)) },
                labelResId = R.string.profile_about,
                onClick = {}
            )
        }

        // ---------- Impact stats ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = stringResource(R.string.currency_rm, profile.mealsRescued * 1.58),
                label = stringResource(R.string.profile_stat_money_saved)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${"%.1f".format(impact.kgFoodSaved)}kg",
                label = stringResource(R.string.profile_stat_co2_reduced)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${impact.mealsRescued}",
                label = stringResource(R.string.profile_stat_meals_rescued)
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: @Composable () -> Unit,
    labelResId: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(labelResId),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .padding(end = 12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, value: String, label: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}