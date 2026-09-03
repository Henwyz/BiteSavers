package com.example.bitesavers.business.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.business.profile.data.BusinessEditTab
import com.example.bitesavers.business.profile.logic.BusinessProfileViewModel // 👈 Imports from .logic
import com.example.bitesavers.customer.profile.logic.SubmissionState

@Composable
fun BusinessProfileEditScreen(
    onBackClick: () -> Unit,
    onSubmitted: (isBusinessDetails: Boolean) -> Unit,
    viewModel: BusinessProfileViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val accountDraft by viewModel.accountDraft.collectAsStateWithLifecycle()
    val accountErrors by viewModel.accountErrors.collectAsStateWithLifecycle()

    val businessDraft by viewModel.businessDraft.collectAsStateWithLifecycle()
    val businessErrors by viewModel.businessErrors.collectAsStateWithLifecycle()
    val hasPendingBusinessEdit by viewModel.hasPendingBusinessEdit.collectAsStateWithLifecycle()

    val submissionState by viewModel.submissionState.collectAsStateWithLifecycle()
    val showNoChangesDialog by viewModel.showNoChangesDialog.collectAsStateWithLifecycle()
    val showTncDialog by viewModel.showTncDialog.collectAsStateWithLifecycle()
    val showPendingWarningDialog by viewModel.showPendingWarningDialog.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.initEditScreen()
    }

    LaunchedEffect(submissionState) {
        if (submissionState is SubmissionState.Success) {
            val isBusiness = selectedTab == BusinessEditTab.BUSINESS
            viewModel.resetSubmissionState()
            onSubmitted(isBusiness)
        }
    }

    // 1. "No Changes" Alert Dialog
    if (showNoChangesDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissNoChangesDialog() },
            title = { Text(stringResource(R.string.ngo_no_changes_title)) },
            text = { Text(stringResource(R.string.ngo_no_changes_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissNoChangesDialog() }) {
                    Text(stringResource(R.string.ngo_dialog_ok))
                }
            }
        )
    }

    // 2. Terms & Conditions Dialog
    if (showTncDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissTncDialog() },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.business_edit_tnc_title))
                    IconButton(onClick = { viewModel.dismissTncDialog() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close))
                    }
                }
            },
            text = { Text(stringResource(R.string.business_edit_tnc_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.agreeToTermsAndSubmit() }) {
                    Text(stringResource(R.string.ngo_tnc_dialog_confirm))
                }
            }
        )
    }

    // 3. Pending Edit Warning Dialog
    if (showPendingWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPendingWarningDialog() },
            title = { Text(stringResource(R.string.business_pending_edit_dialog_title)) },
            text = { Text(stringResource(R.string.business_pending_edit_dialog_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissPendingWarningDialog() }) {
                    Text(stringResource(R.string.ngo_dialog_ok))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
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
                stringResource(R.string.business_edit_details_title),
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Slider Toggle Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                BusinessEditTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .selectable(selected = isSelected, onClick = { viewModel.selectTab(tab) })
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            tab.displayLabel,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ================== TAB 1: ACCOUNT DETAILS ==================
            if (selectedTab == BusinessEditTab.ACCOUNT) {
                Text(
                    stringResource(R.string.business_edit_account_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                BusinessFormField(
                    label = stringResource(R.string.business_edit_name_label),
                    value = accountDraft.name,
                    placeholder = "Enter your username",
                    errorMessage = accountErrors.name,
                    onValueChange = viewModel::updateAccountName,
                    onBlur = { viewModel.onAccountBlur("name") }
                )

                BusinessFormField(
                    label = stringResource(R.string.business_edit_email_label),
                    value = accountDraft.email,
                    placeholder = "e.g. owner@example.com",
                    errorMessage = accountErrors.email,
                    keyboardType = KeyboardType.Email,
                    onValueChange = viewModel::updateAccountEmail,
                    onBlur = { viewModel.onAccountBlur("email") }
                )

                BusinessFormField(
                    label = "New Password (optional)",
                    value = accountDraft.password,
                    placeholder = "Leave blank to keep unchanged",
                    errorMessage = accountErrors.password,
                    isPassword = true,
                    onValueChange = viewModel::updateAccountPassword,
                    onBlur = { viewModel.onAccountBlur("password") }
                )
            }

            // ================== TAB 2: BUSINESS DETAILS ==================
            if (selectedTab == BusinessEditTab.BUSINESS) {
                if (hasPendingBusinessEdit) {
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
                            stringResource(R.string.business_details_pending_banner),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Text(
                    stringResource(R.string.business_edit_store_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                BusinessFormField(
                    label = stringResource(R.string.business_name_label),
                    value = businessDraft.businessName,
                    placeholder = "e.g. Uncle Ong Bakery",
                    errorMessage = businessErrors.businessName,
                    onValueChange = viewModel::updateBusinessName,
                    onBlur = { viewModel.onBusinessBlur("businessName", businessDraft.businessName) }
                )

                BusinessFormField(
                    label = stringResource(R.string.business_address_label),
                    value = businessDraft.address,
                    placeholder = "Store address",
                    errorMessage = businessErrors.address,
                    onValueChange = viewModel::updateBusinessAddress,
                    onBlur = { viewModel.onBusinessBlur("address", businessDraft.address) }
                )

                BusinessFormField(
                    label = stringResource(R.string.business_phone_label),
                    value = businessDraft.phone,
                    placeholder = "e.g. +60 3-1950 5239",
                    errorMessage = businessErrors.phone,
                    keyboardType = KeyboardType.Phone,
                    onValueChange = viewModel::updateBusinessPhone,
                    onBlur = { viewModel.onBusinessBlur("phone", businessDraft.phone) }
                )

                BusinessFormField(
                    label = stringResource(R.string.business_operating_hours_title),
                    value = businessDraft.operatingHours,
                    placeholder = "e.g. 8:30 AM - 9:00 PM",
                    errorMessage = businessErrors.operatingHours,
                    onValueChange = viewModel::updateBusinessOperatingHours,
                    onBlur = { viewModel.onBusinessBlur("operatingHours", businessDraft.operatingHours) }
                )

                BusinessFormField(
                    label = "Clean-up Hours (NGO Pickup Only)",
                    value = businessDraft.cleanupHours,
                    placeholder = "e.g. 9:30 PM - 10:30 PM",
                    errorMessage = businessErrors.cleanupHours,
                    onValueChange = viewModel::updateBusinessCleanupHours,
                    onBlur = { viewModel.onBusinessBlur("cleanupHours", businessDraft.cleanupHours) }
                )

                BusinessFormField(
                    label = stringResource(R.string.ngo_field_reason_for_change),
                    value = businessDraft.reasonForChange,
                    placeholder = stringResource(R.string.ngo_placeholder_reason_for_change),
                    errorMessage = businessErrors.reasonForChange,
                    singleLine = false,
                    onValueChange = viewModel::updateBusinessReasonForChange,
                    onBlur = { viewModel.onBusinessBlur("reasonForChange", businessDraft.reasonForChange) }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = businessDraft.agreedToTerms,
                        onCheckedChange = viewModel::updateAgreedToTerms
                    )
                    Text(stringResource(R.string.ngo_agree_terms), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (submissionState is SubmissionState.Error) {
                Text(
                    (submissionState as SubmissionState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // Action Button
        Button(
            onClick = {
                if (selectedTab == BusinessEditTab.ACCOUNT) {
                    viewModel.saveAccountDetails()
                } else {
                    viewModel.submitBusinessDetails()
                }
            },
            enabled = submissionState !is SubmissionState.Submitting,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(52.dp)
        ) {
            if (submissionState is SubmissionState.Submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onSecondary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (selectedTab == BusinessEditTab.ACCOUNT)
                        "Save Account Details"
                    else
                        "Change Business Details",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BusinessFormField(
    label: String,
    value: String,
    placeholder: String,
    errorMessage: String?,
    onValueChange: (String) -> Unit,
    onBlur: () -> Unit,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Text(
        label,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        },
        isError = errorMessage != null,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState -> if (!focusState.isFocused) onBlur() }
    )
    if (errorMessage != null) {
        Text(
            errorMessage,
            color = MaterialTheme.colorScheme.error,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
    Spacer(Modifier.height(12.dp))
}