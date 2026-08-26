package com.example.bitesavers.customer.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R

/**
 * MVP placeholder content — NOT a real legal privacy policy. Written to be
 * demo-appropriate and honest about what the app actually does; swap for
 * real policy text if this ever ships beyond the assignment.
 */
@Composable
fun PrivacySecurityScreen(onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        ProfileSubScreenHeader(title = stringResource(R.string.profile_privacy_security), onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            PolicySection(
                title = stringResource(R.string.privacy_data_collected_title),
                body = stringResource(R.string.privacy_data_collected_body)
            )
            PolicySection(
                title = stringResource(R.string.privacy_data_use_title),
                body = stringResource(R.string.privacy_data_use_body)
            )
            PolicySection(
                title = stringResource(R.string.privacy_data_sharing_title),
                body = stringResource(R.string.privacy_data_sharing_body)
            )
            PolicySection(
                title = stringResource(R.string.privacy_account_security_title),
                body = stringResource(R.string.privacy_account_security_body),
                showDivider = false
            )
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String, showDivider: Boolean = true) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (showDivider) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

/** Shared header used by all the simple profile sub-screens (back button + title on a green band). */
@Composable
fun ProfileSubScreenHeader(title: String, onBackClick: () -> Unit) {
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
            title,
            color = MaterialTheme.colorScheme.onSecondary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
