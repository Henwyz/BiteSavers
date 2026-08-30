package com.example.bitesavers.login.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.login.logic.LoginViewModel
import com.example.bitesavers.ui.theme.BiteSaverColors
import com.example.bitesavers.ui.theme.BiteSaversTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (isBusiness: Boolean) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BiteSaverColors.HeaderGreen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
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
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.TextOnDark
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.login_tagline),
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.welcome_back),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Email Field
                    Text(
                        text = stringResource(R.string.login_email_label),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mail),
                                contentDescription = stringResource(R.string.cd_email_icon),
                                tint = BiteSaverColors.HeaderGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        isError = viewModel.emailError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BiteSaverColors.SoftGreen,
                            unfocusedContainerColor = BiteSaverColors.SoftGreen,
                            focusedBorderColor = if (viewModel.emailError != null) Color.Red else BiteSaverColors.PrimaryGreen,
                            unfocusedBorderColor = if (viewModel.emailError != null) Color.Red else BiteSaverColors.PrimaryGreen
                        ),
                        singleLine = true
                    )

                    if (viewModel.emailError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.emailError!!,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Text(
                        text = stringResource(R.string.login_password_label),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_password),
                                contentDescription = stringResource(R.string.cd_password_icon),
                                tint = BiteSaverColors.HeaderGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (viewModel.passwordVisible) R.drawable.ic_show else R.drawable.ic_hide
                                    ),
                                    contentDescription = stringResource(R.string.cd_toggle_password),
                                    tint = BiteSaverColors.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (viewModel.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        isError = viewModel.passwordError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BiteSaverColors.SoftGreen,
                            unfocusedContainerColor = BiteSaverColors.SoftGreen,
                            focusedBorderColor = if (viewModel.passwordError != null) Color.Red else BiteSaverColors.PrimaryGreen,
                            unfocusedBorderColor = if (viewModel.passwordError != null) Color.Red else BiteSaverColors.PrimaryGreen
                        ),
                        singleLine = true
                    )

                    if (viewModel.passwordError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.passwordError!!,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account Type Dropdown
                    Text(
                        text = stringResource(R.string.account_type_label),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = viewModel.isDropdownExpanded,
                        onExpandedChange = { viewModel.toggleDropdown(it) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = viewModel.selectedRole,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_home),
                                    contentDescription = stringResource(R.string.cd_account_icon),
                                    tint = BiteSaverColors.HeaderGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.isDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
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
                            expanded = viewModel.isDropdownExpanded,
                            onDismissRequest = { viewModel.toggleDropdown(false) }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.role_consumer)) },
                                onClick = {
                                    viewModel.updateSelectedRole("Consumer")
                                    viewModel.toggleDropdown(false)
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.role_business)) },
                                onClick = {
                                    viewModel.updateSelectedRole("Business")
                                    viewModel.toggleDropdown(false)
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
                            text = stringResource(R.string.forgot_password),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BiteSaverColors.PrimaryGreen,
                            modifier = Modifier.clickable { /* Handle click */ }
                        )
                    }

                    // Login Button
                    Button(
                        onClick = {
                            viewModel.login { isBusiness ->
                                onLoginSuccess(isBusiness)
                            }
                        },
                        enabled = !viewModel.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BiteSaverColors.PrimaryGreen
                        )
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                color = BiteSaverColors.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.log_in_button),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BiteSaverColors.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Navigate to Sign Up Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.no_account_yet),
                            fontSize = 13.sp,
                            color = BiteSaverColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.sign_up_free),
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
    BiteSaversTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToSignUp = {}
        )
    }
}