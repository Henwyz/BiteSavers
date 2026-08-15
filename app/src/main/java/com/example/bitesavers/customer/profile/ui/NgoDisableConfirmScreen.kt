package com.example.bitesavers.customer.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.customer.profile.logic.ProfileViewModel

@Composable
fun NgoDisableConfirmScreen(
    onCancelClick: () -> Unit,
    onDisabled: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    var showDisabledDialog by remember { mutableStateOf(false) }

    if (showDisabledDialog) {
        AlertDialog(
            onDismissRequest = { }, // force acknowledgement via the button
            title = { Text(stringResource(R.string.ngo_disabled_dialog_title)) },
            text = { Text(stringResource(R.string.ngo_disabled_dialog_message)) },
            confirmButton = {
                TextButton(onClick = onDisabled) {
                    Text(stringResource(R.string.ngo_dialog_ok))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancelClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.cd_navigate_back),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
            Text(
                stringResource(R.string.ngo_disable_title),
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(36.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.ngo_disable_confirm_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.ngo_disable_confirm_message),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    viewModel.disableNgoAccount()
                    showDisabledDialog = true
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.ngo_disable_disable_button), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onCancelClick,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.ngo_disable_cancel_button))
            }
        }
    }
}
