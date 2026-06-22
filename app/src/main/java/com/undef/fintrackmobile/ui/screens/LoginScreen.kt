package com.undef.fintrackmobile.ui.screens

import androidx.compose.runtime.Composable
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.components.AuthForm

/**
 * 2️⃣ JETPACK COMPOSE - Composición de Componentes
 * LoginScreen demuestra la reutilización de componentes mediante 'AuthForm'.
 * Implementa 'State Hoisting' al delegar los eventos onLogin y onRegister.
 */
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
        onPrimary = { _, email, password, _, _ -> onLogin(email, password) },
        onSecondary = onRegister,
    )
}
