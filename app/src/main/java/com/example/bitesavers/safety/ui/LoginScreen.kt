package com.example.bitesavers.safety.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaverColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (isBusiness: Boolean) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Consumer") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Outer Column with top alignment & light background
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BiteSaverColors.HeaderGreen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Aligns content starting from the top
    ) {
        Spacer(Modifier.height(20.dp))
        // Top Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.food_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(1.dp)
            )

            // Dark green overlay using BiteSaverColors.HeaderGreen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BiteSaverColors.HeaderGreen.copy(alpha = 0.75f))
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_leaf),
                        contentDescription = null,
                        tint = BiteSaverColors.DiscountOrange,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bite Saver",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.TextOnDark
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Save food. Save money. Save Earth",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BiteSaverColors.TextOnDark.copy(alpha = 0.9f)
                )
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BiteSaverColors.SoftGreen),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = BiteSaverColors.OffWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                // Inner Column to stack items vertically with shared padding
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Welcome Back \uD83D\uDC4B",     // the \UD83D blabla that one is 👋
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Email Address",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Mail,
                                contentDescription = "Email",
                                tint = BiteSaverColors.HeaderGreen
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BiteSaverColors.SoftGreen,      //Inside background color when clicked/typing
                            unfocusedContainerColor = BiteSaverColors.SoftGreen,    //Inside background color when not selected
                            focusedBorderColor = BiteSaverColors.PrimaryGreen,      //Borderline color when clicked/typing
                            unfocusedBorderColor = BiteSaverColors.PrimaryGreen     //Borderline color when not selected
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = BiteSaverColors.HeaderGreen
                            )
                        },
                        trailingIcon = {        // for the eyes icon
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = BiteSaverColors.TextSecondary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BiteSaverColors.SoftGreen,
                            unfocusedContainerColor = BiteSaverColors.SoftGreen,
                            focusedBorderColor = BiteSaverColors.PrimaryGreen,
                            unfocusedBorderColor = BiteSaverColors.PrimaryGreen
                        ),
                        singleLine = true

                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Field 3: Account Type Dropdown Label
                    Text(
                        text = "Account Type",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedRole,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = BiteSaverColors.HeaderGreen
                                )
                            },
                            trailingIcon = {
                                // Standard animated dropdown arrow icon
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor() // Crucial: Anchors the menu directly to this text field
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BiteSaverColors.SoftGreen,
                                unfocusedContainerColor = BiteSaverColors.SoftGreen,
                                focusedBorderColor = BiteSaverColors.PrimaryGreen,
                                unfocusedBorderColor = BiteSaverColors.PrimaryGreen
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Consumer") },
                                onClick = {
                                    selectedRole = "Consumer"
                                    isDropdownExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                            DropdownMenuItem(
                                text = { Text("Business") },
                                onClick = {
                                    selectedRole = "Business"
                                    isDropdownExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Forgot password ?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BiteSaverColors.PrimaryGreen,
                            modifier = Modifier.clickable { /* Handle click */ }
                        )
                    }

                    Button(
                        onClick = { onLoginSuccess(selectedRole == "Business") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BiteSaverColors.PrimaryGreen
                        )
                    ) {
                        Text(
                            text = "Log In",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BiteSaverColors.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sign Up Footer Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No account yet? ",
                            fontSize = 13.sp,
                            color = BiteSaverColors.TextSecondary
                        )
                        Text(
                            text = "Sign up free",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BiteSaverColors.PrimaryGreen,
                            modifier = Modifier.clickable { onNavigateToSignUp() }
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }



    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    com.example.bitesavers.ui.theme.BiteSaversTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToSignUp = {}
        )
    }
}