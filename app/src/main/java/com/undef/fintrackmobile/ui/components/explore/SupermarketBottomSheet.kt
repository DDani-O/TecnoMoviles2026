package com.undef.fintrackmobile.ui.components.explore

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.viewmodel.SupermarketExplore

/**
 * SupermarketBottomSheet: Detalle del supermercado mostrado en una hoja modal.
 * Sigue la arquitectura declarativa y modular.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupermarketBottomSheet(
    supermarket: SupermarketExplore,
    onDismiss: () -> Unit,
    sheetState: SheetState,
) {
    val colors = FintrackTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.neutralWhite,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(colors.celestePale)
            )
        }
    ) {
        SupermarketDetailContent(supermarket = supermarket)
    }
}

@Composable
private fun SupermarketDetailContent(
    supermarket: SupermarketExplore,
) {
    val colors = FintrackTheme.colors
    val context = LocalContext.current
    
    val accent = remember(supermarket.name) {
        when {
            supermarket.name.contains("Carrefour", ignoreCase = true) -> colors.supermarketCarrefourAccent
            supermarket.name.contains("Coto", ignoreCase = true) -> colors.supermarketCotoAccent
            supermarket.name.contains("Jumbo", ignoreCase = true) -> colors.supermarketJumboAccent
            else -> colors.celesteDeep
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supermarket.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
                Text(
                    text = supermarket.shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.celesteInk.copy(alpha = 0.6f)
                )
            }
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = accent.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Storefront, null, tint = accent, modifier = Modifier.size(28.dp))
                }
            }
        }

        // Card de Información Básica
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.celesteMist.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.celestePale)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoItem(icon = Icons.Default.LocationOn, text = supermarket.location, tint = accent)
                
                /**
                 * Mostramos el horario directamente desde la API para evitar repeticiones de prefijos
                 * (Ya que la API de Córdoba ya incluye el estado 'Abierto' o 'Cerrado').
                 */
                InfoItem(
                    icon = Icons.Default.AccessTime, 
                    text = supermarket.hours, 
                    tint = colors.neutralDarkGray
                )

                InfoItem(
                    icon = Icons.Default.Star,
                    text = stringResource(R.string.rating_format, supermarket.rating),
                    tint = colors.medalGold
                )
            }
        }

        // Listas de Promociones y Pagos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionList(
                title = stringResource(R.string.supermarket_promos_title),
                items = supermarket.promotions,
                modifier = Modifier.weight(1f),
                color = accent
            )
            SectionList(
                title = stringResource(R.string.supermarket_payments_title),
                items = supermarket.paymentMethods,
                modifier = Modifier.weight(1f),
                color = colors.celesteDeep
            )
        }

        // Botón Ver en Mapas
        Button(
            onClick = {
                // Intent para abrir Google Maps con la ubicación específica del supermercado
                val gmmIntentUri = "geo:0,0?q=${supermarket.location}".toUri()
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                try {
                    context.startActivity(mapIntent)
                } catch (_: Exception) {
                    // Fallback al navegador si no está Maps instalado
                    val webIntent = Intent(Intent.ACTION_VIEW, "https://www.google.com/maps/search/${supermarket.location}".toUri())
                    context.startActivity(webIntent)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.explore_map_title),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
