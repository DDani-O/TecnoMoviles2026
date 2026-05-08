package com.fintrack.mobile.ui.components.records

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintrack.mobile.ui.theme.FintrackBrandColors
import com.fintrack.mobile.ui.theme.FintrackTheme
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.viewmodel.PeriodFilter
import com.fintrack.mobile.ui.viewmodel.ProductRank

/**
 * StatsSummaryCard: Muestra el total gastado, promedio y cantidad de tickets del periodo.
 */
@Composable
fun StatsSummaryCard(
    selectedPeriod: PeriodFilter,
    selectedMonthIndex: Int,
    selectedYear: Int,
    totalSpent: Long,
    average: Long,
    ticketCount: Int,
    currencyCode: String,
) {
    val colors = FintrackTheme.colors
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.celestePale),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when(selectedPeriod) {
                    PeriodFilter.WEEK -> "Total de la semana"
                    PeriodFilter.MONTH -> {
                        val months = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
                        "Total de ${months[selectedMonthIndex]} $selectedYear"
                    }
                    PeriodFilter.YEAR -> "Total del año $selectedYear"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = colors.celesteInk
            )
            Text(
                text = formatCurrency(totalSpent, currencyCode),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp
                ),
                color = colors.celesteDeep
            )
            HorizontalDivider(color = colors.celesteSoft.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = formatCurrency(average, currencyCode), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.celesteInk)
                    Text(text = "Promedio", style = MaterialTheme.typography.bodyMedium, color = colors.celesteInk.copy(alpha = 0.7f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = ticketCount.toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.celesteInk)
                    Text(text = "Tickets", style = MaterialTheme.typography.bodyMedium, color = colors.celesteInk.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun PeriodFilterRow(
    selectedPeriod: PeriodFilter,
    onSelected: (PeriodFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PeriodFilterButton(
            label = "Semana", 
            selected = selectedPeriod == PeriodFilter.WEEK
        ) { onSelected(PeriodFilter.WEEK) }
        
        PeriodFilterButton(
            label = "Mes", 
            selected = selectedPeriod == PeriodFilter.MONTH
        ) { onSelected(PeriodFilter.MONTH) }
        
        PeriodFilterButton(
            label = "Año", 
            selected = selectedPeriod == PeriodFilter.YEAR
        ) { onSelected(PeriodFilter.YEAR) }
    }
}

@Composable
fun SpecificMonthSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthYearSelected: (Int, Int) -> Unit
) {
    val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    val colors = FintrackTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(colors.celestePale.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthYearSelected(selectedMonth, selectedYear - 1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Año anterior", tint = colors.celesteDeep)
            }
            Text(text = selectedYear.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.celesteDeep)
            IconButton(onClick = { onMonthYearSelected(selectedMonth, selectedYear + 1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Año siguiente", tint = colors.celesteDeep)
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            itemsIndexed(months) { index, month ->
                val isSelected = index == selectedMonth
                Surface(
                    onClick = { onMonthYearSelected(index, selectedYear) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) colors.celesteDeep else colors.neutralWhite,
                    modifier = Modifier.width(60.dp)
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(text = month, style = MaterialTheme.typography.labelLarge, color = if (isSelected) colors.neutralWhite else colors.celesteInk, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
fun SpecificYearSelector(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    val colors = FintrackTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(colors.celestePale.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onYearSelected(selectedYear - 1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Año anterior", tint = colors.celesteDeep)
        }
        Text(text = "Año $selectedYear", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.celesteDeep)
        IconButton(onClick = { onYearSelected(selectedYear + 1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Año siguiente", tint = colors.celesteDeep)
        }
    }
}

@Composable
private fun PeriodFilterButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = FintrackTheme.colors
    Button(
        onClick = onClick,
        modifier = Modifier.height(44.dp).padding(horizontal = 2.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (selected) colors.celesteDeep else colors.celestePale, contentColor = if (selected) colors.neutralWhite else colors.celesteInk),
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
    }
}

@Composable
fun SupermarketPieChartCard(
    distribution: List<Pair<String, Long>>,
    currencyCode: String,
) {
    val totalSpent = distribution.sumOf { it.second }.toFloat().coerceAtLeast(1f)
    val colors = FintrackTheme.colors

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.celesteMist),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.size(140.dp).weight(1f), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    distribution.forEach { (name, amount) ->
                        val sweepAngle = (amount.toFloat() / totalSpent) * 360f
                        drawArc(color = getSupermarketPastelColor(name, colors), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true)
                        startAngle += sweepAngle
                    }
                }
            }

            Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                distribution.take(4).forEach { (name, amount) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(getSupermarketPastelColor(name, colors)))
                        Column {
                            Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = formatCurrency(amount, currencyCode), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductPodiumCard(
    ranking: List<ProductRank>
) {
    if (ranking.isEmpty()) return
    val colors = FintrackTheme.colors

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.celestePale),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                if (ranking.size >= 2) PodiumStep(rank = ranking[1], position = 2, height = 80.dp, color = colors.medalSilver)
                if (ranking.isNotEmpty()) PodiumStep(rank = ranking[0], position = 1, height = 120.dp, color = colors.medalGold)
                if (ranking.size >= 3) PodiumStep(rank = ranking[2], position = 3, height = 60.dp, color = colors.medalBronze)
            }

            if (ranking.size > 3) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ranking.drop(3).forEachIndexed { index, item ->
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.neutralWhite.copy(alpha = 0.5f)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = (index + 4).toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(text = "x${item.quantity}", fontWeight = FontWeight.Bold, color = colors.celesteDeep)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumStep(rank: ProductRank, position: Int, height: Dp, color: Color) {
    val colors = FintrackTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = rank.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(80.dp))
        Box(modifier = Modifier.width(70.dp).height(height).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(color), contentAlignment = Alignment.Center) {
            Text(text = position.toString(), style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold), color = colors.neutralWhite.copy(alpha = 0.8f))
        }
        Text(text = "x${rank.quantity}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

private fun getSupermarketPastelColor(name: String, colors: FintrackBrandColors): Color {
    return when {
        name.contains("Carrefour", ignoreCase = true) -> colors.pastelBlue
        name.contains("Coto", ignoreCase = true) -> colors.pastelRed
        name.contains("Jumbo", ignoreCase = true) -> colors.pastelGreen
        else -> colors.celesteSoft
    }
}
