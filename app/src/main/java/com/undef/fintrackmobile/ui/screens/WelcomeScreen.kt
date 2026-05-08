package com.undef.fintrackmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.components.PrimarySecondaryActions
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * WelcomeScreen: Pantalla de bienvenida para usuarios no autenticados.
 * Presenta la propuesta de valor de la app con un diseño limpio y moderno.
 */
@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FintrackTheme.colors
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.celesteMist
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icono representativo con fondo pastel
            Surface(
                modifier = Modifier.size(160.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = colors.celestePale
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        colorFilter = ColorFilter.tint(colors.celesteDeep),
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Textos de bienvenida
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colors.celesteInk,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.celesteInk.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Acciones de navegación
            PrimarySecondaryActions(
                primaryLabelRes = R.string.action_login,
                secondaryLabelRes = R.string.action_register,
                onPrimary = onLogin,
                onSecondary = onRegister,
                modifier = Modifier.fillMaxWidth(),
                buttonModifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
