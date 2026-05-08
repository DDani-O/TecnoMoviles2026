package com.fintrack.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.theme.FintrackTheme
import com.fintrack.mobile.ui.viewmodel.*
import com.fintrack.mobile.ui.components.explore.*
import kotlinx.coroutines.delay

/**
 * ExploreScreen: Pantalla de exploración rediseñada con una estética divertida y colorida.
 * Sigue el patrón visual de HomeScreen y RecordsScreen usando colores pastel.
 * Los componentes están ubicados en el orden exacto solicitado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = FintrackTheme.colors
    var textoBusqueda by rememberSaveable { mutableStateOf("") }

    Surface(modifier = modifier.fillMaxSize(), color = colors.celesteMist) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Encabezado con título y subtítulo elegante
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.explore_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.celesteDeep,
                )
                Text(
                    text = "Encuentra las mejores ofertas y supermercados cerca de ti",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.celesteInk.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }

            // 2. Buscador optimizado
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("¿Qué buscamos hoy, genio?", color = colors.celesteInk.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.celesteDeep) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.celesteBase,
                    unfocusedBorderColor = colors.celesteSoft.copy(alpha = 0.5f),
                    focusedContainerColor = colors.neutralWhite,
                    unfocusedContainerColor = colors.neutralWhite,
                    cursorColor = colors.celesteDeep
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val current = state) {
                is ExploreUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.celesteDeep)
                    }
                }
                is ExploreUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = current.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
                is ExploreUiState.Exito -> {
                    ContenidoExplore(datos = current.datos)
                }
            }
        }
    }
}

@Composable
private fun ContenidoExplore(datos: DatosExplore) {
    val colors = FintrackTheme.colors

    // 1. Optimizamos el filtrado para evitar crear nuevas listas innecesariamente
    val seccionesFiltradas = remember(datos.seccionesOfertas) {
        datos.seccionesOfertas.filter { it.titulo != "🕒 Últimas ofertas agregadas" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 3. Mapa de locales cercanos (con key para estabilidad)
        item(key = "seccion_mapa") {
            SeccionMapa()
        }

        // Banner de Evento (con key para estabilidad)
        item(key = "banner_evento") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.pastelBlue.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(2.dp, colors.pastelBlue)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Celebration, contentDescription = null, tint = colors.celesteDeep)
                    Text(
                        text = "¡Atención! El 10/6 Feria de Descuentos con todo al 50%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.celesteInk
                    )
                }
            }
        }

        // 4. Sección de Noticias
        if (datos.noticias.isNotEmpty()) {
            item(key = "seccion_noticias") {
                SeccionNoticiasExplore(noticias = datos.noticias)
            }
        }

        // 5. Catálogo de Supermercados
        item(key = "seccion_supermercados") {
            SeccionSupermercados(supermercados = datos.supermercados)
        }

        // 6. Carruseles de Ofertas (usamos keys únicos basados en el título)
        items(
            items = seccionesFiltradas,
            key = { it.titulo }
        ) { seccion ->
            SeccionCarruselOfertasConMovimiento(seccion = seccion)
        }

        // 7. Sugerencias Personalizadas
        if (datos.sugerencias.isNotEmpty()) {
            item(key = "seccion_sugerencias") {
                SeccionSugerenciasExplore(sugerencias = datos.sugerencias)
            }
        }

        item(key = "footer_explore") {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "¡Eso es todo por ahora, ahorrador experto! 💸",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.celesteInk.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun SeccionMapa() {
    val colors = FintrackTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "📍 Locales cercanos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.celesteInk
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, colors.celesteDeep.copy(alpha = 0.1f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.mapa),
                contentDescription = "Mapa de locales cercanos",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun SeccionNoticiasExplore(noticias: List<NoticiaExplore>) {
    val colors = FintrackTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        noticias.forEach { noticia ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.pastelRed.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.pastelRed.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = colors.pastelRed)
                    Column {
                        Text(text = noticia.titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = noticia.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeccionSupermercados(supermercados: List<SupermercadoExplore>) {
    val colors = FintrackTheme.colors
    var seleccionadoId by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val alTocarItem = remember {
        { id: Int -> seleccionadoId = id }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "🛒 Supermercados amigos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = colors.celesteInk
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(
                items = supermercados,
                key = { it.id }
            ) { superm ->
                SupermercadoCard(
                    supermercado = superm,
                    estaSeleccionado = seleccionadoId == superm.id,
                    alTocar = { alTocarItem(superm.id) }
                )
            }
        }

        if (seleccionadoId != null) {
            supermercados.find { it.id == seleccionadoId }?.let { seleccionado ->
                SupermercadoBottomSheet(
                    supermercado = seleccionado,
                    onDismiss = { seleccionadoId = null },
                    sheetState = sheetState
                )
            }
        }
    }
}

@Composable
private fun SeccionCarruselOfertasConMovimiento(seccion: SeccionOfertasExplore) {
    val colors = FintrackTheme.colors
    val listState = rememberLazyListState()
    
    // Efecto de movimiento optimizado
    LaunchedEffect(key1 = seccion.titulo) {
        while (true) {
            delay(4000)
            // Solo animar si la lista tiene items y el usuario no está interactuando
            if (seccion.items.isNotEmpty() && !listState.isScrollInProgress) {
                val nextIndex = (listState.firstVisibleItemIndex + 1) % seccion.items.size
                listState.animateScrollToItem(nextIndex)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = seccion.titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = colors.celesteInk
        )
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(
                items = seccion.items,
                key = { it.nombreProducto + it.precio } // Key compuesta si no hay ID
            ) { item ->
                TarjetaOfertaExplore(item = item)
            }
        }
    }
}

@Composable
private fun TarjetaOfertaExplore(item: OfertaItemExplore) {
    val colors = FintrackTheme.colors
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.neutralWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.celestePale)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = colors.pastelGreenPale,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = item.tienda,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.pastelGreenDeep
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.nombreProducto,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.precio,
                style = MaterialTheme.typography.titleLarge,
                color = colors.celesteDeep,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¡Aprovechá! ⚡",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SeccionSugerenciasExplore(sugerencias: List<SugerenciaExplore>) {
    val colors = FintrackTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "✨ Recomendados solo para vos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.celesteInk
        )
        sugerencias.forEach { sugerencia ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.pastelGreenPale.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.pastelGreenDeep.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = colors.pastelGreenDeep
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = colors.neutralWhite)
                        }
                    }
                    Column {
                        Text(text = sugerencia.titulo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = colors.pastelGreenDeep)
                        Text(text = sugerencia.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

