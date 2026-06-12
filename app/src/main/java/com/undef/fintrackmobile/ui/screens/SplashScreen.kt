package com.undef.fintrackmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.navigation.FintrackDestination
import com.undef.fintrackmobile.ui.navigation.Routes
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import kotlinx.coroutines.delay

/**
 * SplashScreen: Pantalla de carga inicial que gestiona la redirección
 * según el estado de autenticación del usuario.
 */
@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    onFinished: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FintrackTheme.colors

    LaunchedEffect(isLoggedIn) {
        // Un breve retraso para dar tiempo a la animación y carga de preferencias
        delay(1200)
        val destination = if (isLoggedIn) {
            FintrackDestination.Home.route
        } else {
            Routes.WELCOME
        }
        onFinished(destination)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.celesteMist
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icono de marca con estilo circular pastel
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = colors.celestePale,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Título de la App
            Text(
                text = stringResource(R.string.splash_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colors.celesteInk
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtítulo / Eslogan
            Text(
                text = stringResource(R.string.splash_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.celesteInk.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Indicador de carga personalizado
            CircularProgressIndicator(
                color = colors.celesteDeep,
                trackColor = colors.celestePale,
                strokeWidth = 4.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
