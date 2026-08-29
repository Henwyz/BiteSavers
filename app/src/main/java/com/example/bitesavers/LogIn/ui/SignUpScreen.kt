package com.example.bitesavers.LogIn.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitesavers.R
import com.example.bitesavers.LogIn.logic.SignUpViewModel
import com.example.bitesavers.ui.theme.BiteSaverColors

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = viewModel(),
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit
){
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BiteSaverColors.HeaderGreen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .size(40.dp)
                    .background(BiteSaverColors.White, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = BiteSaverColors.HeaderGreen
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = stringResource(R.string.create_account),
                color = BiteSaverColors.White,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 24.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BiteSaverColors.SoftGreen)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(16.dp))

            RoleSegmentedToggle(
                isBusiness = viewModel.isBusiness,
                onRoleSelected = { viewModel.updateIsBusiness(it) }
            )

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BiteSaverColors.OffWhite),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)) {

                    // Full Name
                    Text(
                        text = stringResource(R.string.full_name),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.fullName,
                        onValueChange = { viewModel.updateFullName(it) },
                        placeholder = { Text(if (viewModel.isBusiness) "BiteSaver Cafe" else "Sarah Tan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        isError = viewModel.fullNameError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BiteSaverColors.SoftGreen,
                            unfocusedContainerColor = BiteSaverColors.SoftGreen,
                            focusedBorderColor = if (viewModel.fullNameError != null) Color.Red else BiteSaverColors.PrimaryGreen,
                            unfocusedBorderColor = if (viewModel.fullNameError != null) Color.Red else BiteSaverColors.PrimaryGreen
                        ),
                        singleLine = true
                    )
                    viewModel.fullNameError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(text = error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }

                    Spacer(Modifier.height(15.dp))

                    // Email ADdress
                    Text(
                        text = stringResource(R.string.email_address),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        singleLine = true,
                        placeholder = { Text("sarah@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        isError = viewModel.emailError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BiteSaverColors.SoftGreen,
                            unfocusedContainerColor = BiteSaverColors.SoftGreen,
                            focusedBorderColor = if (viewModel.emailError != null) Color.Red else BiteSaverColors.PrimaryGreen,
                            unfocusedBorderColor = if (viewModel.emailError != null) Color.Red else BiteSaverColors.PrimaryGreen
                        )
                    )
                    viewModel.emailError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(text = error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }

                    Spacer(Modifier.height(15.dp))

                    // Contact number
                    Text(
                        text = stringResource(R.string.contact_number),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.phoneNumber,
                        onValueChange = { viewModel.updatePhoneNumber(it) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = { Text("012-3456789") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        isError = viewModel.phoneError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BiteSaverColors.SoftGreen,
                            unfocusedContainerColor = BiteSaverColors.SoftGreen,
                            focusedBorderColor = if (viewModel.phoneError != null) Color.Red else BiteSaverColors.PrimaryGreen,
                            unfocusedBorderColor = if (viewModel.phoneError != null) Color.Red else BiteSaverColors.PrimaryGreen
                        )
                    )
                    viewModel.phoneError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(text = error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }

                    Spacer(Modifier.height(15.dp))

                    // Password
                    Text(
                        text = stringResource(R.string.password),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        placeholder = { Text("min 8 characters") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = BiteSaverColors.HeaderGreen
                                )
                            }
                        },
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
                    viewModel.passwordError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(text = error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }

                    Spacer(Modifier.height(15.dp))

                    // Password that confirm
                    Text(
                        text = stringResource(R.string.confirm_password),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BiteSaverColors.HeaderGreen,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.confirmPassword,
                        onValueChange = { viewModel.updateConfirmPassword(it) },
                        placeholder = { Text("min 8 characters") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        isError = viewModel.confirmPasswordError != null,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = BiteSaverColors.HeaderGreen
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BiteSaverColors.SoftGreen,
                            unfocusedContainerColor = BiteSaverColors.SoftGreen,
                            focusedBorderColor = if (viewModel.confirmPasswordError != null) Color.Red else BiteSaverColors.PrimaryGreen,
                            unfocusedBorderColor = if (viewModel.confirmPasswordError != null) Color.Red else BiteSaverColors.PrimaryGreen
                        ),
                        singleLine = true
                    )
                    viewModel.confirmPasswordError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(text = error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = viewModel.termsAccepted,
                            onCheckedChange = { viewModel.updateTermsAccepted(it) },
                        )

                        Text(
                            stringResource(R.string.terms_agreement),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            stringResource(R.string.terms_policy),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = BiteSaverColors.HeaderGreen,
                            modifier = Modifier.clickable {
                                onNavigateToTerms()
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.validateAndRegister {
                                onSignUpSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BiteSaverColors.PrimaryGreen,
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.create_account),
                            fontSize = 18.sp,
                            color = BiteSaverColors.White
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.already_have_account),
                            fontSize = 13.sp,
                            color = BiteSaverColors.HeaderGreen
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.sign_in),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BiteSaverColors.HeaderGreen,
                            modifier = Modifier.clickable { onNavigateToLogin() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleSegmentedToggle(
    isBusiness: Boolean,
    onRoleSelected: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(BiteSaverColors.White, RoundedCornerShape(50))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val consumerBg by animateColorAsState(
            if (!isBusiness) BiteSaverColors.ChipGreen else Color.Transparent, label = "ConsumerBg"
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50))
                .background(consumerBg)
                .clickable { onRoleSelected(false) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🛒 Consumer",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BiteSaverColors.HeaderGreen
            )
        }

        val businessBg by animateColorAsState(
            if (isBusiness) BiteSaverColors.ChipGreen else Color.Transparent, label = "BusinessBg"
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50))
                .background(businessBg)
                .clickable { onRoleSelected(true) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🏪 Business",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BiteSaverColors.HeaderGreen
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    com.example.bitesavers.ui.theme.BiteSaversTheme {
        SignUpScreen(
            onSignUpSuccess = {},
            onNavigateToLogin = {},
            onNavigateToTerms = {}
        )
    }
}