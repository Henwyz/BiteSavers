package com.example.bitesavers.business.temperature.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R

@Composable
fun AddBoxScreen(
    onNavigateBack: () -> Unit,
    onUnitAdded: (boxCode: String, storageType: String) -> Unit
) {
    var boxCode by remember { mutableStateOf("") }
    var storageType by remember { mutableStateOf("Refrigerator") } // Default type

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        // Top Header Section
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
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = "Add Storage Box",
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Box(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Configure ESP32 Sensor Unit",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Box Code / Name Field
                    OutlinedTextField(
                        value = boxCode,
                        onValueChange = { boxCode = it },
                        label = { Text("Box Code / Name (e.g., Fridge-01)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Storage Type",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Selection buttons for Storage Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TypeSelectionCard(
                            title = "Refrigerator",
                            isSelected = storageType.equals("Refrigerator", ignoreCase = true),
                            onClick = { storageType = "Refrigerator" },
                            modifier = Modifier.weight(1f)
                        )
                        TypeSelectionCard(
                            title = "Hot Box",
                            isSelected = storageType.equals("Hot Box", ignoreCase = true),
                            onClick = { storageType = "Hot Box" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Save Button
                Button(
                    onClick = {
                        if (boxCode.isNotBlank()) {
                            onUnitAdded(boxCode.trim(), storageType)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = boxCode.isNotBlank()
                ) {
                    Text(
                        text = "Save & Connect Unit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TypeSelectionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}