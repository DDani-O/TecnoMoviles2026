package com.undef.fintrackmobile.ui.components.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.ui.components.BorderedCard

/**
 * OfferCardState: Estado de datos para las tarjetas de oferta.
 */
data class OfferCardState(
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    @param:StringRes val detailRes: Int,
    val accent: Color,
)

/**
 * HomeOfferCarousel: Carrusel horizontal de ofertas destacadas.
 */
@Composable
fun HomeOfferCarousel(
    items: List<OfferCardState>,
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        state = state,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(end = 16.dp),
    ) {
        items(items, key = { it.titleRes }) { offer ->
            OfferCard(offer = offer)
        }
    }
}

@Composable
private fun OfferCard(offer: OfferCardState) {
    BorderedCard(
        modifier = Modifier.fillMaxWidth(0.84f),
        accentColor = offer.accent,
        cornerRadius = 22f,
        containerAlpha = 0.16f,
        content = {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = stringResource(offer.titleRes), style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(offer.subtitleRes), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stringResource(offer.detailRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    )
}
