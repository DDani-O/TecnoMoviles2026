package com.fintrack.mobile.ui.components.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.theme.FintrackTheme
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.viewmodel.HistoryStat

/**
 * HistoryStatCard: Tarjeta que muestra una métrica histórica (ej. ahorro total, compras totales).
 */
@Composable
fun HistoryStatCard(
    stat: HistoryStat,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val colors = FintrackTheme.colors
    Card(
        modifier = modifier
            .fillMaxWidth(0.72f)
            .border(
                width = 1.dp,
                color = colors.indicatorBorder,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = colors.indicatorBg),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(stat.titleRes), style = MaterialTheme.typography.labelMedium)

            val valueText = when {
                stat.valueCents != null -> formatCurrency(stat.valueCents, currencyCode)
                stat.valueCount != null -> stat.valueCount.toString()
                else -> stat.valueString ?: stringResource(R.string.home_stats_no_data)
            }

            Text(text = valueText, style = MaterialTheme.typography.titleLarge)
            val subtitle = stat.subtitleRes?.let { stringResource(it) } ?: stat.subtitle ?: ""
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
