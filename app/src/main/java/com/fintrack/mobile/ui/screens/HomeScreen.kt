package com.fintrack.mobile.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.data.local.entity.PurchaseEntity
import com.fintrack.mobile.ui.components.TitleSubtitleCard
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.util.formatDate
import com.fintrack.mobile.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    displayName: String,
    currencyCode: String,
    viewModel: HomeViewModel,
) {
    val purchases by viewModel.recentPurchases.collectAsStateWithLifecycle()
    val monthlyTotal by viewModel.monthlyTotalCents.collectAsStateWithLifecycle()
    val greetingName = displayName.ifBlank { stringResource(R.string.default_user_name) }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.home_greeting, greetingName),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(R.string.home_welcome),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            item {
                SummaryCard(
                    totalFormatted = formatCurrency(monthlyTotal, currencyCode),
                )
            }

            item {
                SectionHeader(
                    title = R.string.home_recent_history,
                    subtitle = R.string.home_view_all,
                )
            }

            items(purchases.take(3)) { purchase ->
                PurchaseRow(purchase, currencyCode)
            }

            item {
                SectionHeader(title = R.string.home_expenses_category, subtitle = null)
                CategorySection()
            }

            item {
                SectionHeader(title = R.string.home_top_supermarkets, subtitle = null)
                TopSupermarkets()
            }

            item {
                TitleSubtitleCard(
                    title = stringResource(R.string.home_promos),
                    subtitle = stringResource(R.string.home_promos_hint),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentPadding = 16.dp,
                    titleStyle = MaterialTheme.typography.titleMedium,
                    subtitleStyle = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(totalFormatted: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.home_month_summary),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_total_spent, totalFormatted),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(16.dp))
            BarChart()
        }
    }
}

@Composable
private fun BarChart() {
    val values = listOf(0.25f, 0.55f, 0.4f, 0.7f, 0.6f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        values.forEach { value ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barHeight = size.height * value
                    drawRect(
                        color = Color(0xFF2E7D32),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - barHeight),
                        size = androidx.compose.ui.geometry.Size(size.width, barHeight),
                    )
                }
            }
        }
    }
}

@Composable
private fun PurchaseRow(purchase: PurchaseEntity, currencyCode: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = purchase.supermarketName,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_purchase_date, formatDate(purchase.dateMillis)),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatCurrency(purchase.totalCents, currencyCode),
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

private data class CategoryStat(
    @param:StringRes val labelRes: Int,
    val percent: Float,
    val color: Color,
)

@Composable
private fun CategorySection() {
    val categories = listOf(
        CategoryStat(R.string.category_food, 0.45f, Color(0xFF2E7D32)),
        CategoryStat(R.string.category_cleaning, 0.2f, Color(0xFF66BB6A)),
        CategoryStat(R.string.category_home, 0.15f, Color(0xFF81C784)),
        CategoryStat(R.string.category_other, 0.2f, Color(0xFFBDBDBD)),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        categories.forEach { stat ->
            Column {
                Text(text = stringResource(stat.labelRes))
                LinearProgressIndicator(
                    progress = { stat.percent },
                    color = stat.color,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TopSupermarkets() {
    val supermarkets = listOf(
        R.string.supermarket_carrefour,
        R.string.supermarket_coto,
        R.string.supermarket_jumbo,
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        supermarkets.forEachIndexed { index, nameRes ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = stringResource(nameRes))
                    Text(text = stringResource(R.string.home_rank_value, index + 1))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    @StringRes title: Int,
    @StringRes subtitle: Int?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(title), style = MaterialTheme.typography.titleMedium)
        subtitle?.let {
            Text(text = stringResource(it), style = MaterialTheme.typography.bodySmall)
        }
    }
}
