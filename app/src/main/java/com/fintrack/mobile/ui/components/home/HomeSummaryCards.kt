package com.fintrack.mobile.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.theme.FintrackTheme
import com.fintrack.mobile.ui.util.formatCurrency

/**
 * HomeSummaryCard: Muestra el resumen de gastos mensuales con un gráfico de barras semanal.
 */
@Composable
fun HomeSummaryCard(
    totalFormatted: String,
    averageFormatted: String,
    weeklyStats: List<Long>,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    val colors = FintrackTheme.colors
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, colors.celesteDeep.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.neutralWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.home_month_summary),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = colors.celesteDeep
                )
                Text(
                    text = stringResource(R.string.home_month_summary_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(0.5f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryMetric(label = stringResource(R.string.home_month_total_label), value = totalFormatted, emphasized = true)
                    SummaryMetric(label = stringResource(R.string.home_month_average_label), value = averageFormatted)
                }
                Box(modifier = Modifier.weight(0.5f)) {
                    BarChart(
                        values = weeklyStats.map { it.toFloat() },
                        labels = (1..weeklyStats.size).map { "S$it" },
                        amounts = weeklyStats.map { formatCurrency(it, currencyCode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, emphasized: Boolean = false) {
    val colors = FintrackTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = colors.celesteDeep.copy(alpha = 0.75f))
        Text(
            text = value,
            style = if (emphasized) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (emphasized) colors.celesteDeep else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BarChart(values: List<Float>, labels: List<String>, amounts: List<String>) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val colors = FintrackTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            values.forEachIndexed { index, value ->
                val ratio = value / maxValue
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = amounts[index],
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.celesteBase,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((ratio * 90).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(colors.celesteBase),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = labels[index], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
