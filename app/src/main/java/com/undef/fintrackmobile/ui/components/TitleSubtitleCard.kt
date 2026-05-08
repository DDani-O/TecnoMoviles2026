package com.undef.fintrackmobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * TitleSubtitleCard: Tarjeta simple diseñada para mostrar un título y un subtítulo.
 * Permite personalizar colores, estilos de texto y rellenos.
 */
@Composable
fun TitleSubtitleCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentPadding: Dp = 12.dp,
    titleStyle: TextStyle? = null,
    subtitleStyle: TextStyle? = null,
) {
    val colors = containerColor?.let { CardDefaults.cardColors(containerColor = it) } ?: CardDefaults.cardColors()
    val resolvedTitleStyle = titleStyle ?: MaterialTheme.typography.titleSmall
    val resolvedSubtitleStyle = subtitleStyle ?: MaterialTheme.typography.bodySmall

    Card(modifier = modifier.fillMaxWidth(), colors = colors) {
        Column(modifier = Modifier.padding(contentPadding)) {
            Text(text = title, style = resolvedTitleStyle)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = resolvedSubtitleStyle)
        }
    }
}
