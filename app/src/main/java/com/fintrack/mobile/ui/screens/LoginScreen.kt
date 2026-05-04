package com.fintrack.mobile.ui.screens

import androidx.compose.runtime.Composable
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.components.AuthForm

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
) {
    AuthForm(
        titleRes = R.string.login_title,
        primaryLabelRes = R.string.action_login,
        secondaryLabelRes = R.string.action_register,
        onPrimary = onLogin,
        onSecondary = onRegister
    )
}
