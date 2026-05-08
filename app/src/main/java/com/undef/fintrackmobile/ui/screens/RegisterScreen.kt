package com.undef.fintrackmobile.ui.screens

import androidx.compose.runtime.Composable
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.components.AuthForm

@Composable
fun RegisterScreen(
    onRegister: (String, String, String?, String?) -> Unit,
    onLogin: () -> Unit,
) {
    AuthForm(
        titleRes = R.string.register_title,
        primaryLabelRes = R.string.action_register,
        secondaryLabelRes = R.string.action_login,
        isRegister = true,
        onPrimary = onRegister,
        onSecondary = onLogin,
    )
}
