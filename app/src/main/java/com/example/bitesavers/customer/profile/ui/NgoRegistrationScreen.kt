package com.example.bitesavers.customer.profile.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.customer.profile.data.NgoCauseCategory
import com.example.bitesavers.customer.profile.data.NgoRegistrationType
import com.example.bitesavers.customer.profile.logic.NgoFormMode
import com.example.bitesavers.customer.profile.logic.ProfileViewModel
import com.example.bitesavers.customer.profile.logic.SubmissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NgoRegistrationScreen(
    mode: NgoFormMode,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val application by viewModel.ngoApplication.collectAsStateWithLifecycle()
    val fieldErrors by viewModel.fieldErrors.collectAsStateWithLifecycle()
    val submissionState by viewModel.submissionState.collectAsStateWithLifecycle()
    val showTncDialog by viewModel.showTncDialog.collectAsStateWithLifecycle()
    val showNoChangesDialog by viewModel.showNoChangesDialog.collectAsStateWithLifecycle()
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCertificate(uri, getFileDisplayName(context, uri))
        }
    }

    LaunchedEffect(submissionState) {
        if (submissionState is SubmissionState.Success) {
            onSubmitted()
            viewModel.resetSubmissionState()
        }
    }

    if (showTncDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissTncDialog() },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.ngo_tnc_dialog_title))
                    IconButton(
                        onClick = { viewModel.dismissTncDialog() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close))
                    }
                }
            },
            text = {
                Text(
                    if (mode == NgoFormMode.REGISTER)
                        stringResource(R.string.ngo_tnc_dialog_message_register)
                    else
                        stringResource(R.string.ngo_tnc_dialog_message_edit)
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.agreeToTermsAndSubmit() }) {
                    Text(stringResource(R.string.ngo_tnc_dialog_confirm))
                }
            }
        )
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                text = if (mode == NgoFormMode.REGISTER)
                    stringResource(R.string.ngo_registration_title)
                else
                    stringResource(R.string.ngo_edit_title),
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
            Text(
                stringResource(R.string.ngo_registration_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            FormField(
                label = stringResource(R.string.ngo_field_org_name),
                value = application.organizationName,
                placeholder = stringResource(R.string.ngo_placeholder_org_name),
                errorMessage = fieldErrors.organizationName,
                onValueChange = viewModel::updateOrganizationName,
                onBlur = { viewModel.onFieldBlur("organizationName", application.organizationName) }
            )

            Text(
                stringResource(R.string.ngo_field_registration_type),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)
            )
            RegistrationTypeToggle(
                selected = application.registrationType,
                onSelect = viewModel::updateRegistrationType
            )
            Spacer(Modifier.height(12.dp))

            FormField(
                label = stringResource(R.string.ngo_field_registration_number),
                value = application.registrationNumber,
                placeholder = if (application.registrationType == NgoRegistrationType.SSM)
                    stringResource(R.string.ngo_placeholder_registration_ssm)
                else
                    stringResource(R.string.ngo_placeholder_registration_ros),
                errorMessage = fieldErrors.registrationNumber,
                onValueChange = viewModel::updateRegistrationNumber,
                onBlur = { viewModel.onFieldBlur("registrationNumber", application.registrationNumber) }
            )
            FormField(
                label = stringResource(R.string.ngo_field_contact_name),
                value = application.contactPersonName,
                placeholder = stringResource(R.string.ngo_placeholder_contact_name),
                errorMessage = fieldErrors.contactPersonName,
                onValueChange = viewModel::updateContactPersonName,
                onBlur = { viewModel.onFieldBlur("contactPersonName", application.contactPersonName) }
            )
            FormField(
                label = stringResource(R.string.ngo_field_contact_email),
                value = application.contactEmail,
                placeholder = stringResource(R.string.ngo_placeholder_email),
                errorMessage = fieldErrors.contactEmail,
                onValueChange = viewModel::updateContactEmail,
                onBlur = { viewModel.onFieldBlur("contactEmail", application.contactEmail) }
            )
            FormField(
                label = stringResource(R.string.ngo_field_contact_phone),
                value = application.contactPhone,
                placeholder = stringResource(R.string.ngo_placeholder_phone),
                errorMessage = fieldErrors.contactPhone,
                onValueChange = viewModel::updateContactPhone,
                onBlur = { viewModel.onFieldBlur("contactPhone", application.contactPhone) }
            )

            Text(
                stringResource(R.string.ngo_field_cause_category),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)
            )
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = it },
                modifier = Modifier.padding(bottom = if (fieldErrors.causeCategory != null) 2.dp else 12.dp)
            ) {
                OutlinedTextField(
                    value = application.causeCategory?.displayLabel ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = {
                        Text(
                            stringResource(R.string.ngo_select_category_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    isError = fieldErrors.causeCategory != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    colors = ngoFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    NgoCauseCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayLabel) },
                            onClick = {
                                viewModel.updateCauseCategory(category)
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }
            if (fieldErrors.causeCategory != null) {
                Text(
                    fieldErrors.causeCategory!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            FormField(
                label = stringResource(R.string.ngo_field_address),
                value = application.address,
                placeholder = stringResource(R.string.ngo_placeholder_address),
                errorMessage = fieldErrors.address,
                onValueChange = viewModel::updateAddress,
                onBlur = { viewModel.onFieldBlur("address", application.address) }
            )

            // Kept in both REGISTER and EDIT — only the read-only NgoDetailsScreen
            // hides this in favor of a plain "Certificate Verified" line.
            CertificateUploadField(
                fileName = application.certificateFileName,
                onPickFile = { pdfPickerLauncher.launch("application/pdf") }
            )
            Spacer(Modifier.height(12.dp))

            // Reason for change — EDIT mode only, deliberately placed last.
            if (mode == NgoFormMode.EDIT) {
                FormField(
                    label = stringResource(R.string.ngo_field_reason_for_change),
                    value = application.reasonForChange,
                    placeholder = stringResource(R.string.ngo_placeholder_reason_for_change),
                    errorMessage = fieldErrors.reasonForChange,
                    onValueChange = viewModel::updateReasonForChange,
                    onBlur = { viewModel.onFieldBlur("reasonForChange", application.reasonForChange) },
                    singleLine = false
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = application.agreedToTerms,
                    onCheckedChange = viewModel::updateAgreedToTerms
                )
                Text(stringResource(R.string.ngo_agree_terms), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (submissionState is SubmissionState.Error) {
                Text(
                    (submissionState as SubmissionState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        Button(
            onClick = { viewModel.submitNgoApplication(mode) },
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
                    text = if (mode == NgoFormMode.REGISTER)
                        stringResource(R.string.ngo_submit_application)
                    else
                        stringResource(R.string.ngo_submit_edit),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    placeholder: String,
    errorMessage: String?,
    onValueChange: (String) -> Unit,
    onBlur: () -> Unit,
    singleLine: Boolean = true
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
        colors = ngoFieldColors(),
        shape = RoundedCornerShape(16.dp),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        modifier = Modifier
            .fillMaxWidth()
            // Validation triggers here, on blur — not on every keystroke.
            // That's what avoids "invalid email" flashing while the user is
            // still mid-typing the first time through the field.
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

@Composable
private fun RegistrationTypeToggle(
    selected: NgoRegistrationType,
    onSelect: (NgoRegistrationType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        NgoRegistrationType.entries.forEach { type ->
            val isSelected = type == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .selectable(selected = isSelected, onClick = { onSelect(type) })
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    type.displayLabel,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CertificateUploadField(fileName: String?, onPickFile: () -> Unit) {
    Text(
        stringResource(R.string.ngo_field_certificate),
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)
    )
    Surface(
        onClick = onPickFile,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (fileName != null) Icons.Filled.CheckCircle else Icons.Filled.UploadFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(10.dp))
            Text(
                fileName ?: stringResource(R.string.ngo_certificate_placeholder),
                color = if (fileName != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ngoFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    // errorBorderColor / errorLabelColor deliberately left as M3 defaults
    // (they resolve to MaterialTheme.colorScheme.error) — that's what gives
    // the red border automatically when isError = true.
)

private fun getFileDisplayName(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}
