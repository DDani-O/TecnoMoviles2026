package com.undef.fintrackmobile.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * LabeledTextField: Campo de texto personalizado con etiqueta y estilo moderno.
 * Sigue la línea estética pastel y usa bordes redondeados consistentes.
 * Soporta modo solo lectura con acción de click (ideal para selectores de fecha).
 */
@Composable
fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    @StringRes placeholderRes: Int,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    isPassword: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val colors = FintrackTheme.colors
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { 
                Text(
                    text = stringResource(labelRes),
                    fontWeight = FontWeight.Medium
                ) 
            },
            placeholder = { 
                Text(
                    text = stringResource(placeholderRes),
                    color = colors.neutralLightGray
                ) 
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.celesteDeep,
                unfocusedBorderColor = colors.celesteSoft,
                focusedLabelColor = colors.celesteDeep,
                unfocusedLabelColor = colors.celesteInk,
                cursorColor = colors.celesteDeep,
                focusedContainerColor = colors.neutralWhite,
                unfocusedContainerColor = colors.celesteMist,
                disabledBorderColor = colors.celesteSoft,
                disabledLabelColor = colors.celesteInk,
                disabledTextColor = colors.celesteInk
            ),
            singleLine = true,
            readOnly = readOnly,
            enabled = onClick == null // Si hay onClick, lo desactivamos para que no tome foco
        )
        
        // Capa transparente para capturar el click si es un selector
        if (onClick != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onClick)
            )
        }
    }
}
