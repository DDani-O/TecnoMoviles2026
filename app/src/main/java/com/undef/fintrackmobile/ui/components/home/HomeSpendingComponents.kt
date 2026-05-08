package com.undef.fintrackmobile.ui.components.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.util.formatCurrency
import com.undef.fintrackmobile.ui.util.getSupermarketColors

/**
 * SupermarketSpendingSection: Barra de progreso comparativa de gastos por supermercado.
 */
@Composable
fun SupermarketSpendingSection(
    stats: List<Pair<String, Long>>,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val maxSpent = stats.maxOfOrNull { it.second }?.toFloat()?.coerceAtLeast(1f) ?: 1f
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        stats.forEach { (name, total) ->
            val ratio = total.toFloat() / maxSpent
            val barColor = getSupermarketColors(name).accentColor

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold)
                    Text(text = formatCurrency(total, currencyCode), style = MaterialTheme.typography.titleMedium, color = barColor, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(CircleShape).background(barColor.copy(alpha = 0.18f))) {
                    Box(modifier = Modifier.fillMaxWidth(ratio).fillMaxSize().clip(CircleShape).background(barColor))
                }
            }
        }
    }
}

/**
 * CategorySpendingSection: Gráfico de torta descriptivo de gastos por categoría.
 */
@Composable
fun CategorySpendingSection(modifier: Modifier = Modifier) {
    val colors = FintrackTheme.colors
    val categories = remember(colors) {
        listOf(
            CategoryStat(R.string.category_food, 0.45f, colors.categoryFood),
            CategoryStat(R.string.category_cleaning, 0.2f, colors.categoryCleaning),
            CategoryStat(R.string.category_home, 0.15f, colors.categoryHome),
            CategoryStat(R.string.category_other, 0.2f, colors.categoryOther),
        )
    }

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(0.55f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { stat ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(modifier = Modifier.size(16.dp), shape = CircleShape, color = stat.color) {}
                    Text(text = stringResource(stat.labelRes), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(text = "${(stat.percent * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            }
        }
        Box(modifier = Modifier.weight(0.45f).height(180.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2.4f
                val centerX = size.width / 2
                val centerY = size.height / 2
                var startAngle = -90f

                categories.forEach { stat ->
                    val sweepAngle = stat.percent * 360f
                    drawArc(
                        color = stat.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = androidx.compose.ui.geometry.Offset(centerX - radius, centerY - radius),
                    )
                    startAngle += sweepAngle
                }
            }
        }
    }
}

private data class CategoryStat(
    @get:StringRes val labelRes: Int,
    val percent: Float,
    val color: Color,
)
