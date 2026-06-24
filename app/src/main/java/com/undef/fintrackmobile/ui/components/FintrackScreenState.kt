package com.undef.fintrackmobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * FintrackScreenState: Componente de alto nivel para manejar estados comunes de pantalla.
 * Centraliza la visualización de indicadores de carga y mensajes de error.
 */
@Composable
fun FintrackScreenState(
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = FintrackTheme.colors
    
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.celesteDeep)
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.error_prefix, errorMessage),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            content()
        }
    }
}
