package com.undef.fintrackmobile.ui.screens

import androidx.compose.runtime.Composable
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.components.AuthForm

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
) {
    AuthForm(
        titleRes = R.string.login_title,
        primaryLabelRes = R.string.action_login,
        secondaryLabelRes = R.string.action_register,
        isRegister = false,
        onPrimary = { name, email, _, _ -> onLogin(name, email) },
        onSecondary = onRegister,
    )
}
