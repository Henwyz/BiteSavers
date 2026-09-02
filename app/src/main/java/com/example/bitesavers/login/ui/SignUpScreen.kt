package com.example.bitesavers.login.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.bitesavers.login.logic.SignUpViewModel
import com.example.bitesavers.ui.theme.BiteSaversTheme

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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(Modifier.height(10.dp))
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
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.cd_back_button),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = stringResource(R.string.create_account),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(16.dp))

            RoleSegmentedToggle(
                isBusiness = viewModel.isBusiness,
                onRoleSelected = { viewModel.updateIsBusiness(it) }
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.fullName,
                        onValueChange = { viewModel.updateFullName(it) },
                        placeholder = {
                            Text(
                                if (viewModel.isBusiness) {
                                    stringResource(R.string.signup_placeholder_name_business)
                                } else {
                                    stringResource(R.string.signup_placeholder_name_consumer)
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        isError = viewModel.fullNameError != null,
                        colors = signupFieldColors(isError = viewModel.fullNameError != null),
                        singleLine = true
                    )
                    viewModel.fullNameError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }

                    Spacer(Modifier.height(15.dp))

                    // Email Address
                    Text(
                        text = stringResource(R.string.email_address),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.signup_placeholder_email)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        isError = viewModel.emailError != null,
                        colors = signupFieldColors(isError = viewModel.emailError != null)
                    )
                    viewModel.emailError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }

                    Spacer(Modifier.height(15.dp))

                    // Password
                    Text(
                        text = stringResource(R.string.password),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        placeholder = { Text(stringResource(R.string.signup_placeholder_password)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (passwordVisible) R.drawable.ic_show else R.drawable.ic_hide
                                    ),
                                    contentDescription = stringResource(R.string.cd_toggle_password),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        isError = viewModel.passwordError != null,
                        colors = signupFieldColors(isError = viewModel.passwordError != null),
                        singleLine = true
                    )
                    viewModel.passwordError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }

                    Spacer(Modifier.height(15.dp))

                    // Confirm Password
                    Text(
                        text = stringResource(R.string.confirm_password),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.confirmPassword,
                        onValueChange = { viewModel.updateConfirmPassword(it) },
                        placeholder = { Text(stringResource(R.string.signup_placeholder_password)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        isError = viewModel.confirmPasswordError != null,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (confirmPasswordVisible) R.drawable.ic_show else R.drawable.ic_hide
                                    ),
                                    contentDescription = stringResource(R.string.cd_toggle_password),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = signupFieldColors(isError = viewModel.confirmPasswordError != null),
                        singleLine = true
                    )
                    viewModel.confirmPasswordError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = viewModel.termsAccepted,
                            onCheckedChange = { viewModel.updateTermsAccepted(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                checkmarkColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )

                        Text(
                            text = stringResource(R.string.terms_agreement),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = stringResource(R.string.terms_policy),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                onNavigateToTerms()
                            }
                        )
                    }

                    if (viewModel.generalError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = viewModel.generalError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.validateAndRegister {
                                onSignUpSuccess()
                            }
                        },
                        enabled = viewModel.termsAccepted && !viewModel.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.create_account),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.sign_in),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
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
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val consumerBg by animateColorAsState(
            if (!isBusiness) MaterialTheme.colorScheme.primary else Color.Transparent, label = "ConsumerBg"
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
                text = stringResource(R.string.role_toggle_consumer),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (!isBusiness) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val businessBg by animateColorAsState(
            if (isBusiness) MaterialTheme.colorScheme.primary else Color.Transparent, label = "BusinessBg"
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
                text = stringResource(R.string.role_toggle_business),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isBusiness) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun signupFieldColors(isError: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.background,
    unfocusedContainerColor = MaterialTheme.colorScheme.background,
    disabledContainerColor = MaterialTheme.colorScheme.background,
    errorContainerColor = MaterialTheme.colorScheme.background,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    errorTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    errorBorderColor = MaterialTheme.colorScheme.error,
    errorCursorColor = MaterialTheme.colorScheme.error,
    focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
)

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    BiteSaversTheme {
        SignUpScreen(
            onSignUpSuccess = {},
            onNavigateToLogin = {},
            onNavigateToTerms = {}
        )
    }
}