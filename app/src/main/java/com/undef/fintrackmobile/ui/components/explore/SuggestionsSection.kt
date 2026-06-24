package com.undef.fintrackmobile.ui.components.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.viewmodel.SuggestionExplore

/**
 * SuggestionsSection: Sección para mostrar sugerencias personalizadas al usuario en la pantalla de exploración.
 */
@Composable
fun SuggestionsSection(
    suggestions: List<SuggestionExplore>,
    modifier: Modifier = Modifier,
) {
    val colors = FintrackTheme.colors
    Column(modifier = modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        Text(
                            text = suggestion.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.pastelGreenDeep
                        )
                        Text(
                            text = suggestion.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
