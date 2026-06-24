package com.undef.fintrackmobile.ui.components.explore

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * MapSection: Muestra un marcador de posición de mapa para locales cercanos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = FintrackTheme.colors
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.explore_map_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.celesteInk,
        )
        Spacer(modifier = Modifier.height(8.dp))
        val mapsUrlFormat = stringResource(R.string.maps_url_search, "supermercados")
        Card(
            onClick = {
                // Intent para abrir Google Maps buscando supermercados cercanos
                val gmmIntentUri = "geo:0,0?q=supermercados".toUri()
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                
                // Intenta abrir la app de Maps, si no, usa el navegador como fallback
                try {
                    context.startActivity(mapIntent)
                } catch (_: Exception) {
                    val webIntent = Intent(Intent.ACTION_VIEW, mapsUrlFormat.toUri())
                    context.startActivity(webIntent)
                }
            },
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, colors.celesteDeep.copy(alpha = 0.1f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.mapa),
                contentDescription = stringResource(R.string.explore_map_desc),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}
