package com.fintrack.mobile.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * BorderedCard: Una tarjeta con borde de color acentuado y fondo semitransparente.
 * Útil para destacar contenido como noticias u ofertas.
 */
@Composable
fun BorderedCard(
    accentColor: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Float = 16f,
    containerAlpha: Float = 0.12f,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(cornerRadius.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = containerAlpha)),
        shape = RoundedCornerShape(cornerRadius.dp),
    ) {
        content()
    }
}
