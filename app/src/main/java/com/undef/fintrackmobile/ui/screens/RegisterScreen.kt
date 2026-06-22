package com.undef.fintrackmobile.ui.screens

import androidx.compose.runtime.Composable
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.components.AuthForm
import com.undef.fintrackmobile.ui.viewmodel.AuthUiState

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (String, String, String, String, String) -> Unit,
    onLogin: () -> Unit,
) {
    AuthForm(
        titleRes = R.string.register_title,
        primaryLabelRes = R.string.action_register,
        secondaryLabelRes = R.string.action_login,
        isRegister = true,
        isLoading = uiState is AuthUiState.Loading,
        onPrimary = { name, email, password, lastName, birthDate ->
            onRegister(name, email, password, lastName ?: "", birthDate ?: "")
        },
        onSecondary = onLogin,
    )
}
