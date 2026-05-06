package com.fintrack.mobile.ui.screens

import androidx.compose.runtime.Composable
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.components.AuthForm

@Composable
fun RegisterScreen(
    onRegister: (String, String) -> Unit,
    onLogin: () -> Unit,
) {
    AuthForm(
        titleRes = R.string.register_title,
        primaryLabelRes = R.string.action_register,
        secondaryLabelRes = R.string.action_login,
        onPrimary = onRegister,
        onSecondary = onLogin,
    )
}
