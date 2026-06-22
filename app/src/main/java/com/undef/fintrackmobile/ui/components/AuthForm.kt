package com.undef.fintrackmobile.ui.components

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.util.formatDate
import com.undef.fintrackmobile.ui.util.showDatePicker

/**
 * AuthForm: Componente base para formularios de autenticación (Login y Registro).
 * Proporciona una estructura consistente con validación básica y diseño pastel.
 */
@Composable
fun AuthForm(
    @StringRes titleRes: Int,
    @StringRes primaryLabelRes: Int,
    @StringRes secondaryLabelRes: Int,
    isRegister: Boolean = false,
    isLoading: Boolean = false,
    onPrimary: (displayName: String, email: String, password: String, lastName: String?, birthDate: String?) -> Unit,
    onSecondary: () -> Unit,
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    
    var showError by rememberSaveable { mutableStateOf(value = false) }
    val colors = FintrackTheme.colors
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.celesteMist
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // Título de la pantalla
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colors.celesteDeep,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(if (isRegister) R.string.auth_form_register_subtitle else R.string.auth_form_login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.celesteInk.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            // Tarjeta contenedora del formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.neutralWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isRegister) {
                        LabeledTextField(
                            value = displayName,
                            onValueChange = { displayName = it; showError = false },
                            labelRes = R.string.label_name,
                            placeholderRes = R.string.placeholder_name,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        LabeledTextField(
                            value = lastName,
                            onValueChange = { lastName = it; showError = false },
                            labelRes = R.string.label_last_name,
                            placeholderRes = R.string.placeholder_last_name,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    LabeledTextField(
                        value = email,
                        onValueChange = { email = it; showError = false },
                        labelRes = R.string.label_email,
                        placeholderRes = R.string.placeholder_email,
                        keyboardType = KeyboardType.Email,
                        modifier = Modifier.fillMaxWidth()
                    )

                    LabeledTextField(
                        value = password,
                        onValueChange = { password = it; showError = false },
                        labelRes = R.string.label_password,
                        placeholderRes = R.string.placeholder_password,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (isRegister) {
                        // Campo de fecha de nacimiento con selector de calendario (igual que en compras)
                        LabeledTextField(
                            value = birthDate,
                            onValueChange = { birthDate = it; showError = false },
                            labelRes = R.string.label_birth_date,
                            placeholderRes = R.string.placeholder_birth_date,
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            onClick = {
                                showDatePicker(context, System.currentTimeMillis()) { millis ->
                                    birthDate = formatDate(millis)
                                    showError = false
                                }
                            },
                        )
                    }
                    
                    if (showError) {
                        Text(
                            text = stringResource(R.string.auth_error_required_fields),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Acciones principales
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        Log.d("AuthForm", "Primary button clicked. isRegister=$isRegister")
                        val isValid = if (isRegister) {
                            displayName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && birthDate.isNotBlank()
                        } else {
                            email.isNotBlank() && password.isNotBlank()
                        }
                        
                        if (isValid) {
                            onPrimary(displayName, email, password, if (isRegister) lastName else null, if (isRegister) birthDate else null)
                        } else {
                            Log.w("AuthForm", "Validation failed")
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.celesteDeep,
                        contentColor = colors.neutralWhite
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colors.neutralWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(primaryLabelRes),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onSecondary,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.celesteInk
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.celesteSoft)
                ) {
                    Text(
                        text = stringResource(secondaryLabelRes),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
