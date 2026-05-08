package com.fintrack.mobile.ui.components.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.theme.FintrackTheme
import com.fintrack.mobile.ui.viewmodel.SupermercadoExplore

/**
 * SupermercadoCard: Tarjeta estática optimizada para mostrar la información básica del supermercado.
 * Implementa CERO animaciones para asegurar estabilidad visual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupermercadoCard(
    supermercado: SupermercadoExplore,
    estaSeleccionado: Boolean,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FintrackTheme.colors

    // Lógica para determinar logo y acento (sin crear objetos en cada recomposición)
    val (accent, logoRes) = remember(supermercado.nombre) {
        when {
            supermercado.nombre.contains("Carrefour", ignoreCase = true) -> 
                colors.supermarketCarrefourAccent to R.drawable.logo_carrefour
            supermercado.nombre.contains("Coto", ignoreCase = true) -> 
                colors.supermarketCotoAccent to R.drawable.logo_coto
            supermercado.nombre.contains("Jumbo", ignoreCase = true) -> 
                colors.supermarketJumboAccent to R.drawable.logo_jumbo
            else -> 
                colors.celesteBase to R.drawable.ic_launcher_foreground
        }
    }

    Card(
        onClick = alTocar,
        modifier = modifier.size(width = 160.dp, height = 180.dp),
        shape = RoundedCornerShape(24.dp),
        // Fondo blanco sólido para evitar problemas de transparencia
        colors = CardDefaults.cardColors(
            containerColor = colors.neutralWhite,
            disabledContainerColor = colors.neutralWhite
        ),
        // Borde estático para indicar selección
        border = androidx.compose.foundation.BorderStroke(
            width = if (estaSeleccionado) 2.5.dp else 1.dp,
            color = if (estaSeleccionado) accent else colors.celestePale
        ),
        // Elevación fija sin animaciones de pulsación
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 2.dp,
            focusedElevation = 2.dp,
            hoveredElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Contenedor del Logo PNG
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = if (logoRes == R.drawable.ic_launcher_foreground) accent.copy(alpha = 0.1f) else Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "Logo ${supermercado.nombre}",
                        modifier = Modifier.size(if (logoRes == R.drawable.ic_launcher_foreground) 32.dp else 60.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = supermercado.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.celesteInk,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = supermercado.descripcionCorta,
                style = MaterialTheme.typography.labelSmall,
                color = colors.celesteInk.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
