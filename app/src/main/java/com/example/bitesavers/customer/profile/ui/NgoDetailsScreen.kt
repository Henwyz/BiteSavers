package com.example.bitesavers.customer.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    viewModel: ProfileViewModel = viewModel()
) {
    val details by viewModel.activeNgoDetails.collectAsStateWithLifecycle()

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
                    // extra bottom padding so the FAB never sits on top of the last row
                    .padding(20.dp)
                    .padding(bottom = 72.dp)
            ) {
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
                    DetailRow(stringResource(R.string.ngo_field_cause_category), d.causeCategory.displayLabel)
                    DetailRow(stringResource(R.string.ngo_field_address), d.address)
                    DetailRow(
                        stringResource(R.string.ngo_field_certificate),
                        d.certificateFileName ?: stringResource(R.string.ngo_certificate_placeholder)
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onEditClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
            text = { Text(stringResource(R.string.ngo_details_edit_button)) }
        )
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
