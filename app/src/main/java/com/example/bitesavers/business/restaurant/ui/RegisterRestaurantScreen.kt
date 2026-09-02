package com.example.bitesavers.business.restaurant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.business.restaurant.logic.RegisterRestaurantViewModel
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun RegisterRestaurantScreen(
    viewModel: RegisterRestaurantViewModel = viewModel(),
    onRestaurantRegistered: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121A14)), // Dark background matching your theme
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(30.dp))
        Text(
            text = "Register Your Restaurant 🏪",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(16.dp))

        // Scrollable container with dark surface style
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121A14))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Restaurant Name",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA5D6A7)
            )
            OutlinedTextField(
                value = viewModel.restaurantName,
                onValueChange = { viewModel.updateRestaurantName(it) },
                placeholder = { Text("e.g. BiteSaver Cafe", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E2A20),
                    unfocusedContainerColor = Color(0xFF1E2A20),
                    disabledContainerColor = Color(0xFF1E2A20),
                    errorContainerColor = Color(0xFF1E2A20),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFA5D6A7),
                    unfocusedBorderColor = Color(0xFF2C3E30)
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Address",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA5D6A7)
            )
            OutlinedTextField(
                value = viewModel.address,
                onValueChange = { viewModel.updateAddress(it) },
                placeholder = { Text("e.g. 123, Jalan Ampang", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E2A20),
                    unfocusedContainerColor = Color(0xFF1E2A20),
                    disabledContainerColor = Color(0xFF1E2A20),
                    errorContainerColor = Color(0xFF1E2A20),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFA5D6A7),
                    unfocusedBorderColor = Color(0xFF2C3E30)
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Opening Time",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA5D6A7)
            )
            OutlinedTextField(
                value = viewModel.openingTime,
                onValueChange = { viewModel.updateOpeningTime(it) },
                placeholder = { Text("e.g. 09:00 AM", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E2A20),
                    unfocusedContainerColor = Color(0xFF1E2A20),
                    disabledContainerColor = Color(0xFF1E2A20),
                    errorContainerColor = Color(0xFF1E2A20),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFA5D6A7),
                    unfocusedBorderColor = Color(0xFF2C3E30)
                )
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Closing Time",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA5D6A7)
            )
            OutlinedTextField(
                value = viewModel.closingTime,
                onValueChange = { viewModel.updateClosingTime(it) },
                placeholder = { Text("e.g. 10:00 PM", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E2A20),
                    unfocusedContainerColor = Color(0xFF1E2A20),
                    disabledContainerColor = Color(0xFF1E2A20),
                    errorContainerColor = Color(0xFF1E2A20),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFA5D6A7),
                    unfocusedBorderColor = Color(0xFF2C3E30)
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {
                    viewModel.registerRestaurant {
                        onRestaurantRegistered()
                    }
                },
                enabled = viewModel.restaurantName.isNotBlank() && viewModel.address.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA5D6A7),
                    disabledContainerColor = Color(0xFFA5D6A7).copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Save & Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF121A14)
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterRestaurantScreenPreview() {
    BiteSaversTheme {
        RegisterRestaurantScreen(
            onRestaurantRegistered = {}
        )
    }
}