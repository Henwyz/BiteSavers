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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaverColors

@Composable
fun AddBoxScreen(
    onNavigateBack: () -> Unit,
    onUnitAdded: (String, String) -> Unit
) {
    var unitName by remember { mutableStateOf("") }
    var isRefrigerator by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121A14)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(BiteSaverColors.White, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.cd_back_button),
                        tint = BiteSaverColors.HeaderGreen
                    )
                }
                Text(
                    text = stringResource(R.string.add_storage_box),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Box(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.esp32_hardware_setup),
                color = Color(0xFFA5D6A7),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.add_box_description),
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Box/Unit Name Input
            OutlinedTextField(
                value = unitName,
                onValueChange = { unitName = it },
                label = { Text(stringResource(R.string.box_name_hint), color = Color(0xFFB0BEC5)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFA5D6A7),
                    unfocusedBorderColor = Color(0xFF2C3E30)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.storage_category),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Refrigerator / Hot Box Selection Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { isRefrigerator = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRefrigerator) Color(0xFF2E7D32) else Color(0xFF1E2A20)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.refrigerator), color = Color.White)
                }
                Button(
                    onClick = { isRefrigerator = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isRefrigerator) Color(0xFF2E7D32) else Color(0xFF1E2A20)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.hot_box), color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save / Confirm Button
            Button(
                onClick = {
                    if (unitName.isNotBlank()) {
                        val typeText = if (isRefrigerator) "Refrigerator" else "Hot Box"
                        onUnitAdded(unitName.trim(), typeText)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.save_and_connect_unit),
                    color = Color(0xFF121A14),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121A14)
@Composable
fun AddBoxScreenPreview() {
    AddBoxScreen(
        onNavigateBack = {},
        onUnitAdded = { _, _ -> }
    )
}