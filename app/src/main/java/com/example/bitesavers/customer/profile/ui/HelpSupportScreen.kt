package com.example.bitesavers.customer.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bitesavers.R

private data class FaqEntry(val questionRes: Int, val answerRes: Int)

private val FAQ_ENTRIES = listOf(
    FaqEntry(R.string.faq_q_reserve, R.string.faq_a_reserve),
    FaqEntry(R.string.faq_q_missed_pickup, R.string.faq_a_missed_pickup),
    FaqEntry(R.string.faq_q_ngo, R.string.faq_a_ngo),
    FaqEntry(R.string.faq_q_payment_safety, R.string.faq_a_payment_safety)
)

@Composable
fun HelpSupportScreen(onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        ProfileSubScreenHeader(title = stringResource(R.string.profile_help_support), onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                stringResource(R.string.help_faq_title),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FAQ_ENTRIES.forEach { entry ->
                FaqRow(questionRes = entry.questionRes, answerRes = entry.answerRes)
            }

            Spacer(Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.help_contact_title),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ContactRow(icon = Icons.Filled.Email, value = stringResource(R.string.help_contact_email))
                    ContactRow(icon = Icons.Filled.Phone, value = stringResource(R.string.help_contact_phone))
                }
            }
        }
    }
}

@Composable
private fun FaqRow(questionRes: Int, answerRes: Int) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(questionRes),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (expanded) {
            Spacer(Modifier.height(6.dp))
            Text(stringResource(answerRes), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(value, color = MaterialTheme.colorScheme.onBackground)
    }
}
