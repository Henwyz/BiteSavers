package com.example.bitesavers.customer.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.customer.profile.data.NgoStatus
import com.example.bitesavers.customer.profile.data.UserProfileUiModel
import com.example.bitesavers.customer.profile.logic.ProfileViewModel
import com.example.bitesavers.customer.profile.logic.SustainabilityCalculator
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun ProfileScreen(
    onRegisterAsNgoClick: () -> Unit,
    onViewNgoDetailsClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit, // Navigation callback for Payment Methods screen
    onSignOutClick: () -> Unit,
    onPrivacySecurityClick: () -> Unit,
    onHelpSupportClick: () -> Unit,
    onAboutClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadError by viewModel.loadError.collectAsStateWithLifecycle()

    ProfileContent(
        profile = profile,
        isLoading = isLoading,
        loadError = loadError,
        onRegisterAsNgoClick = onRegisterAsNgoClick,
        onViewNgoDetailsClick = onViewNgoDetailsClick,
        onPaymentMethodsClick = onPaymentMethodsClick,
        onSignOutClick = onSignOutClick,
        onPrivacySecurityClick = onPrivacySecurityClick,
        onHelpSupportClick = onHelpSupportClick,
        onAboutClick = onAboutClick
    )
}

@Composable
private fun ProfileContent(
    profile: UserProfileUiModel,
    isLoading: Boolean,
    loadError: String?,
    onRegisterAsNgoClick: () -> Unit,
    onViewNgoDetailsClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onPrivacySecurityClick: () -> Unit,
    onHelpSupportClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val impact = SustainabilityCalculator.calculateImpact(profile.mealsRescued)
    val menuIconTint = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (loadError != null) {
            Text(
                loadError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(12.dp)
            )
        }

        // ---------- Header ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
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
            }

            Spacer(Modifier.height(8.dp))

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
                        text = profile.avatarInitials,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = profile.name,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = profile.email,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = profile.memberSinceLabel,
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
                            painter = painterResource(id = R.drawable.ic_payment),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
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
                    onClick = onPaymentMethodsClick,
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
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_saved),
                        contentDescription = null,
                        tint = menuIconTint,
                        modifier = Modifier.size(16.dp)
                    )
                },
                labelResId = when (profile.ngoStatus) {
                    NgoStatus.NONE -> R.string.profile_register_ngo
                    NgoStatus.PENDING -> R.string.profile_ngo_pending
                    NgoStatus.APPROVED -> R.string.profile_ngo_approved
                },
                onClick = {
                    // Approved NGOs land on the read-only details screen; everyone else goes straight to registration form.
                    if (profile.ngoStatus == NgoStatus.APPROVED) {
                        onViewNgoDetailsClick()
                    } else {
                        onRegisterAsNgoClick()
                    }
                }
            )
            ProfileMenuRow(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_payment),
                        contentDescription = null,
                        tint = menuIconTint,
                        modifier = Modifier.size(16.dp)
                    )
                },
                labelResId = R.string.profile_payment_methods,
                onClick = onPaymentMethodsClick // Trigger payment methods navigation
            )
            ProfileMenuRow(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = null,
                        tint = menuIconTint,
                        modifier = Modifier.size(16.dp)
                    )
                },
                labelResId = R.string.profile_privacy_security,
                onClick = onPrivacySecurityClick
            )
            ProfileMenuRow(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = menuIconTint,
                        modifier = Modifier.size(16.dp)
                    )
                },
                labelResId = R.string.profile_help_support,
                onClick = onHelpSupportClick
            )
            ProfileMenuRow(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notification),
                        contentDescription = null,
                        tint = menuIconTint,
                        modifier = Modifier.size(16.dp)
                    )
                },
                labelResId = R.string.profile_about,
                onClick = onAboutClick
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
                    text = stringResource(labelResId),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
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

