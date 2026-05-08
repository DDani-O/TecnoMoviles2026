package com.undef.fintrackmobile.ui.screens

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
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.viewmodel.*
import com.undef.fintrackmobile.ui.components.explore.*
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
    var searchText by rememberSaveable { mutableStateOf("") }

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
                    text = stringResource(R.string.explore_subtitle_hero),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.celesteInk.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                )
            }

            // 2. Buscador optimizado
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text(stringResource(R.string.explore_search_hint), color = colors.celesteInk.copy(alpha = 0.5f)) },
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
                    cursorColor = colors.celesteDeep,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val current = state) {
                is ExploreUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.celesteDeep)
                    }
                }
                is ExploreUiState.Error -> {
                    val errorMsg = current.messageRes?.let { 
                        stringResource(it, *current.messageArgs.toTypedArray()) 
                    } ?: current.message ?: stringResource(R.string.error_unknown)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMsg, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
                is ExploreUiState.Success -> {
                    ExploreContent(data = current.data)
                }
            }
        }
    }
}

/**
 * ExploreContent: Contenedor de la lista de elementos en la pantalla de exploración.
 */
@Composable
private fun ExploreContent(data: ExploreData) {
    val colors = FintrackTheme.colors

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 3. Mapa de locales cercanos
        item(key = "map_section") {
            MapSection()
        }

        // Banner de Evento
        item(key = "event_banner") {
            ExploreNewsCard(
                title = stringResource(R.string.explore_event_title),
                description = stringResource(R.string.explore_event_desc),
                icon = Icons.Default.Celebration,
                containerColor = colors.pastelBlue,
                contentColor = colors.celesteInk,
                borderColor = colors.pastelBlue,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 4. Sección de Noticias
        if (data.news.isNotEmpty()) {
            item(key = "news_section") {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    data.news.forEach { item ->
                        ExploreNewsCard(
                            title = item.title,
                            description = item.description,
                            icon = Icons.Default.Info,
                            containerColor = colors.pastelRed,
                            contentColor = colors.celesteInk,
                            borderColor = colors.pastelRed
                        )
                    }
                }
            }
        }

        // 5. Catálogo de Supermercados
        item(key = "supermarkets_section") {
            SupermarketsSection(supermarkets = data.supermarkets)
        }

        // 6. Carruseles de Ofertas
        items(
            items = data.offerSections,
            key = { it.title }
        ) { section ->
            OfferCarouselWithMotionSection(section = section)
        }

        // 7. Sugerencias Personalizadas
        if (data.suggestions.isNotEmpty()) {
            item(key = "suggestions_section") {
                SuggestionsSectionExplore(suggestions = data.suggestions)
            }
        }

        item(key = "explore_footer") {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.explore_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.celesteInk.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * MapSection: Muestra un marcador de posición de mapa para locales cercanos.
 */
@Composable
private fun MapSection() {
    val colors = FintrackTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.explore_map_title),
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
                contentDescription = stringResource(R.string.explore_map_desc),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * SupermarketsSection: Muestra una fila horizontal de tarjetas de supermercado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupermarketsSection(supermarkets: List<SupermarketExplore>) {
    val colors = FintrackTheme.colors
    var selectedId by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val onSupermarketClick = remember {
        { id: Int -> selectedId = id }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.explore_supermarkets_title),
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
                items = supermarkets,
                key = { it.id }
            ) { supermarket ->
                SupermarketCard(
                    supermarket = supermarket,
                    isSelected = selectedId == supermarket.id,
                    onClick = { onSupermarketClick(supermarket.id) }
                )
            }
        }

        if (selectedId != null) {
            supermarkets.find { it.id == selectedId }?.let { selected ->
                SupermarketBottomSheet(
                    supermarket = selected,
                    onDismiss = { selectedId = null },
                    sheetState = sheetState,
                )
            }
        }
    }
}

/**
 * OfferCarouselWithMotionSection: Sección de carrusel animado para ofertas.
 */
@Composable
private fun OfferCarouselWithMotionSection(section: OfferSectionExplore) {
    val colors = FintrackTheme.colors
    val listState = rememberLazyListState()

    // Efecto de movimiento optimizado
    LaunchedEffect(key1 = section.title) {
        while (true) {
            delay(4000)
            // Solo animar si la lista tiene items y el usuario no está interactuando
            if (section.items.isNotEmpty() && !listState.isScrollInProgress) {
                val nextIndex = (listState.firstVisibleItemIndex + 1) % section.items.size
                listState.animateScrollToItem(nextIndex)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = section.title,
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
                items = section.items,
                key = { it.title + it.description }
            ) { item ->
                OfferCardExplore(item = item)
            }
        }
    }
}

/**
 * OfferCardExplore: Tarjeta individual para mostrar detalles de una oferta.
 */
@Composable
private fun OfferCardExplore(item: OfferItemExplore) {
    val colors = FintrackTheme.colors
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.neutralWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.celestePale)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Surface(
                color = colors.pastelGreenPale,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = item.store,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.pastelGreenDeep
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colors.celesteDeep
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.celesteInk.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.explore_offer_cta),
                style = MaterialTheme.typography.labelSmall,
                color = colors.celesteSoft,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * SuggestionsSectionExplore: Sección para mostrar sugerencias personalizadas al usuario.
 */
@Composable
private fun SuggestionsSectionExplore(suggestions: List<SuggestionExplore>) {
    val colors = FintrackTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.explore_suggestions_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.celesteInk
        )
        suggestions.forEach { suggestion ->
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
                        Text(text = suggestion.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = colors.pastelGreenDeep)
                        Text(text = suggestion.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
