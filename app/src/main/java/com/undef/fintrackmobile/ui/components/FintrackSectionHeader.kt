package com.undef.fintrackmobile.ui.components
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * FintrackSectionHeader: Encabezado reutilizable para secciones con título y subtítulo opcional.
 * Mantiene la consistencia visual en toda la aplicación (Home, Records, etc.).
 */
@Composable
fun FintrackSectionHeader(
    title: Any,
    modifier: Modifier = Modifier,
    subtitle: Any? = null,
) {
    val colors = FintrackTheme.colors
    
    val titleText = when (title) {
        is Int -> stringResource(title)
        is String -> title
        else -> ""
    }
    
    val subtitleText = when (subtitle) {
        is Int -> stringResource(subtitle)
        is String -> subtitle
        else -> null
    }

    Column(modifier = modifier.padding(horizontal = 4.dp)) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Bold,
            color = colors.celesteInk
        )
        subtitleText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
