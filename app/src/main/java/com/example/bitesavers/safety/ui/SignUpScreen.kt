package com.example.bitesavers.safety.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
){

}


@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    com.example.bitesavers.ui.theme.BiteSaversTheme {
        SignUpScreen(
            onSignUpSuccess = {},
            onNavigateToLogin = {}
        )
    }
}