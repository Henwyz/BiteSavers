package com.example.bitesavers.business.restaurant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R

@Composable
fun PendingApprovalScreen(
    restaurantName: String = "",
    ssmNumber: String = "",
    contactPhone: String = "",
    address: String = "",
    openingTime: String = "",
    closingTime: String = "",
    cleanupEndTime: String = "",
    ssmDocUploaded: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            Text(
                text = stringResource(R.string.restaurant_status_title),
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Box(modifier = Modifier.size(40.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Warning Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "⚠️", fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.pending_review_banner),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                ReadonlyField(label = stringResource(R.string.label_restaurant_name), value = restaurantName)
                Spacer(Modifier.height(16.dp))
                ReadonlyField(label = stringResource(R.string.label_ssm_number), value = ssmNumber)
                Spacer(Modifier.height(16.dp))
                ReadonlyField(label = stringResource(R.string.label_contact_phone), value = contactPhone)
                Spacer(Modifier.height(16.dp))
                ReadonlyField(label = stringResource(R.string.label_address), value = address)
                Spacer(Modifier.height(16.dp))

                // Operating Hours Fields (with correct string resources)
                ReadonlyField(label = stringResource(R.string.opening_time_label), value = openingTime)
                Spacer(Modifier.height(16.dp))
                ReadonlyField(label = stringResource(R.string.closing_time_label), value = closingTime)
                Spacer(Modifier.height(16.dp))
                ReadonlyField(label = stringResource(R.string.cleanup_end_time_label), value = cleanupEndTime)
                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.certificate_of_registration),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (ssmDocUploaded) stringResource(R.string.certificate_verified) else stringResource(R.string.pending_doc_verification),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ReadonlyField(label: String, value: String) {
    val notProvidedText = stringResource(R.string.not_provided)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(text = if (value.isNotBlank()) value else notProvidedText, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
    }
}