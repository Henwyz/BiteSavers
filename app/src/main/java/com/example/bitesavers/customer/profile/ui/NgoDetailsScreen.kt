package com.example.bitesavers.customer.profile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.customer.profile.logic.ProfileViewModel

@Composable
fun NgoDetailsScreen(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDisableClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val details by viewModel.activeNgoDetails.collectAsStateWithLifecycle()
    val hasPendingEdit by viewModel.hasPendingEdit.collectAsStateWithLifecycle()
    var showPendingEditDialog by remember { mutableStateOf(false) }
    var fabMenuExpanded by remember { mutableStateOf(false) }

    if (showPendingEditDialog) {
        AlertDialog(
            onDismissRequest = { showPendingEditDialog = false },
            title = { Text(stringResource(R.string.ngo_pending_edit_dialog_title)) },
            text = { Text(stringResource(R.string.ngo_pending_edit_dialog_message)) },
            confirmButton = {
                TextButton(onClick = { showPendingEditDialog = false }) {
                    Text(stringResource(R.string.ngo_dialog_ok))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.cd_navigate_back),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Text(
                    stringResource(R.string.ngo_details_title),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    // extra bottom padding so the FAB stack never sits on top of the last row
                    .padding(bottom = 140.dp)
            ) {
                if (hasPendingEdit) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Filled.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.ngo_details_pending_banner),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                if (details == null) {
                    // Shouldn't normally happen — this screen is only reachable
                    // once an NGO registration has been approved.
                    Text(
                        stringResource(R.string.ngo_details_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val d = details!!
                    DetailRow(stringResource(R.string.ngo_field_org_name), d.organizationName)
                    DetailRow(stringResource(R.string.ngo_field_registration_type), d.registrationType.displayLabel)
                    DetailRow(stringResource(R.string.ngo_field_registration_number), d.registrationNumber)
                    DetailRow(stringResource(R.string.ngo_field_contact_name), d.contactPersonName)
                    DetailRow(stringResource(R.string.ngo_field_contact_email), d.contactEmail)
                    DetailRow(stringResource(R.string.ngo_field_contact_phone), d.contactPhone)
                    DetailRow(
                        stringResource(R.string.ngo_field_cause_category),
                        d.causeCategory?.displayLabel ?: "-"
                    )
                    DetailRow(stringResource(R.string.ngo_field_address), d.address)

                    // Deliberately just a status badge here — no filename, no
                    // "(PDF)" suffix, no upload control. That stays in the
                    // register/edit form only.
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            stringResource(R.string.ngo_details_certificate_label),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.ngo_details_certificate_verified),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // ---------- Speed-dial FAB: pencil-only main button, reveals
        // "Edit" and "Disable NGO Account" options above it when tapped ----------
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = fabMenuExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            fabMenuExpanded = false
                            if (hasPendingEdit) showPendingEditDialog = true else onEditClick()
                        },
                        containerColor = if (hasPendingEdit)
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            MaterialTheme.colorScheme.secondary,
                        contentColor = if (hasPendingEdit)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSecondary,
                        icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        text = { Text(stringResource(R.string.ngo_details_edit_button)) }
                    )
                    ExtendedFloatingActionButton(
                        onClick = {
                            fabMenuExpanded = false
                            onDisableClick()
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        icon = { Icon(Icons.Filled.Block, contentDescription = null) },
                        text = { Text(stringResource(R.string.ngo_details_disable_button)) }
                    )
                }
            }

            FloatingActionButton(
                onClick = { fabMenuExpanded = !fabMenuExpanded },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.ngo_details_edit_button))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    Spacer(Modifier.height(4.dp))
}
