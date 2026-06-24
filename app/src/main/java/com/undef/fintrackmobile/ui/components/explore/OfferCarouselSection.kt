package com.undef.fintrackmobile.ui.components.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.viewmodel.OfferSectionExplore
import kotlinx.coroutines.delay

/**
 * OfferCarouselSection: Sección de carrusel animado para ofertas en la pantalla de exploración.
 */
@Composable
fun OfferCarouselSection(
    section: OfferSectionExplore,
    modifier: Modifier = Modifier,
) {
    val colors = FintrackTheme.colors
    val listState = rememberLazyListState()

    // Efecto de movimiento automático
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

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
