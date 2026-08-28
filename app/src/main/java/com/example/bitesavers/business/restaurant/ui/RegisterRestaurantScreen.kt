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
import com.example.bitesavers.ui.theme.BiteSaverColors

@Composable
fun RegisterRestaurantScreen(
    viewModel: RegisterRestaurantViewModel = viewModel(),
    onRestaurantRegistered: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BiteSaverColors.HeaderGreen),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(30.dp))
        Text(
            text = "Register Your Restaurant 🏪",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = BiteSaverColors.White
        )
        Spacer(Modifier.height(16.dp))

        // Added verticalScroll so the page scrolls and nothing gets cut off!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BiteSaverColors.OffWhite)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Restaurant Name",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BiteSaverColors.HeaderGreen
            )
            OutlinedTextField(
                value = viewModel.restaurantName,
                onValueChange = { viewModel.updateRestaurantName(it) },
                placeholder = { Text("e.g. BiteSaver Cafe") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BiteSaverColors.SoftGreen,
                    unfocusedContainerColor = BiteSaverColors.SoftGreen,
                    focusedBorderColor = BiteSaverColors.PrimaryGreen,
                    unfocusedBorderColor = BiteSaverColors.PrimaryGreen
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
                color = BiteSaverColors.HeaderGreen
            )
            OutlinedTextField(
                value = viewModel.address,
                onValueChange = { viewModel.updateAddress(it) },
                placeholder = { Text("e.g. 123, Jalan Ampang") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BiteSaverColors.SoftGreen,
                    unfocusedContainerColor = BiteSaverColors.SoftGreen,
                    focusedBorderColor = BiteSaverColors.PrimaryGreen,
                    unfocusedBorderColor = BiteSaverColors.PrimaryGreen
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
                color = BiteSaverColors.HeaderGreen
            )
            OutlinedTextField(
                value = viewModel.openingTime,
                onValueChange = { viewModel.updateOpeningTime(it) },
                placeholder = { Text("e.g. 09:00 AM") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BiteSaverColors.SoftGreen,
                    unfocusedContainerColor = BiteSaverColors.SoftGreen,
                    focusedBorderColor = BiteSaverColors.PrimaryGreen,
                    unfocusedBorderColor = BiteSaverColors.PrimaryGreen
                )
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Closing Time",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BiteSaverColors.HeaderGreen
            )
            OutlinedTextField(
                value = viewModel.closingTime,
                onValueChange = { viewModel.updateClosingTime(it) },
                placeholder = { Text("e.g. 10:00 PM") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BiteSaverColors.SoftGreen,
                    unfocusedContainerColor = BiteSaverColors.SoftGreen,
                    focusedBorderColor = BiteSaverColors.PrimaryGreen,
                    unfocusedBorderColor = BiteSaverColors.PrimaryGreen
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
                    containerColor = BiteSaverColors.PrimaryGreen
                )
            ) {
                Text(
                    text = "Save & Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BiteSaverColors.White
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterRestaurantScreenPreview() {
    com.example.bitesavers.ui.theme.BiteSaversTheme {
        RegisterRestaurantScreen(
            onRestaurantRegistered = {}
        )
    }
}