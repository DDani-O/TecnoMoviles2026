package com.undef.fintrackmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.components.FintrackScreenState
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.viewmodel.*
import com.undef.fintrackmobile.ui.components.explore.*

/**
 * ExploreScreen: Pantalla de exploración que centraliza ofertas y supermercados.
 * Refactorizada para usar FintrackScreenState y componentes modulares.
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
            // Cabecera
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

            // Buscador
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = {
                    Text(
                        stringResource(R.string.explore_search_hint),
                        color = colors.celesteInk.copy(alpha = 0.5f)
                    )
                },
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
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            FintrackScreenState(
                isLoading = state is ExploreUiState.Loading,
                errorMessage = if (state is ExploreUiState.Error) {
                    val current = state as ExploreUiState.Error
                    current.messageRes?.let { stringResource(it, *current.messageArgs.toTypedArray()) }
                        ?: current.message
                } else null
            ) {
                if (state is ExploreUiState.Success) {
                    ExploreContent(data = (state as ExploreUiState.Success).data)
                }
            }
        }
    }
}

/**
 * ExploreContent: Contenedor principal de la información de exploración.
 */
@Composable
private fun ExploreContent(data: ExploreData) {
    val colors = FintrackTheme.colors

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Mapa
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

        // Sección de Noticias
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

        // Catálogo de Supermercados
        item(key = "supermarkets_section") {
            SupermarketsSection(supermarkets = data.supermarkets)
        }

        // Carruseles de Ofertas
        items(
            items = data.offerSections,
            key = { it.title }
        ) { section ->
            OfferCarouselSection(section = section)
        }

        // Sugerencias Personalizadas
        if (data.suggestions.isNotEmpty()) {
            item(key = "suggestions_section") {
                SuggestionsSection(suggestions = data.suggestions)
            }
        }

        // Footer
        item(key = "explore_footer") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
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
 * SupermarketsSection: Muestra una fila horizontal de tarjetas de supermercado.
 * Mantenido aquí para manejar el estado del BottomSheet específico de esta sección.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupermarketsSection(supermarkets: List<SupermarketExplore>) {
    val colors = FintrackTheme.colors
    var selectedId by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    onClick = { selectedId = supermarket.id }
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
