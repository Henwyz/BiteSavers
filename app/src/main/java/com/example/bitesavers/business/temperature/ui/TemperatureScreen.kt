package com.example.bitesavers.business.temperature.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.business.temperature.logic.TemperatureViewModel

@Composable
fun TemperatureScreen(
    viewModel: TemperatureViewModel = viewModel(),
    storeId: String = "",
    onBackClick: () -> Unit,
    onAddUnitClick: () -> Unit = {}
) {
    val currentSelected = viewModel.selectedUnit

    // State to manage the delete confirmation dialog
    var boxToDeleteId by remember { mutableStateOf<String?>(null) }
    var boxToDeleteName by remember { mutableStateOf("") }

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
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.cd_back_button),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = stringResource(R.string.temperature_monitor),
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Box(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddUnitClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Storage Unit",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Live Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.esp32_live_sensor),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.live),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (currentSelected != null) "${currentSelected.currentTemperature}° C" else "--° C",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${currentSelected?.boxCode ?: stringResource(R.string.select_a_unit)} • ${currentSelected?.storageType ?: ""}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.all_connected_units),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Render real storage boxes dynamically
                if (viewModel.units.isEmpty()) {
                    Text(
                        text = "No storage boxes connected yet. Tap + to add one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    viewModel.units.forEach { box ->
                        val isWarning = box.currentTemperature > (box.targetTemperature + 5.0)
                        DetailedUnitCard(
                            name = box.boxCode,
                            type = box.storageType,
                            temp = "${box.currentTemperature}° C",
                            status = if (isWarning) "Warning" else "Normal",
                            isWarning = isWarning,
                            isSelected = box.id == currentSelected?.id,
                            onClick = { viewModel.selectUnit(box) },
                            onDeleteClick = {
                                boxToDeleteId = box.id
                                boxToDeleteName = box.boxCode
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    // Alert message will pop up to notice the user
    if (boxToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { boxToDeleteId = null },
            title = {
                Text(text = "Delete Storage Box?")
            },
            text = {
                Text(text = "Are you sure you want to remove '$boxToDeleteName'? ")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        boxToDeleteId?.let { id ->
                            viewModel.deleteBox(id, storeId)
                        }
                        boxToDeleteId = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { boxToDeleteId = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailedUnitCard(
    name: String,
    type: String,
    temp: String,
    status: String,
    isWarning: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = type,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = temp,
                        color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = status,
                            color = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Box",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}