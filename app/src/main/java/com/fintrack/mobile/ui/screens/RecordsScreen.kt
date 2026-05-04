package com.fintrack.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.data.local.entity.PurchaseEntity
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.util.formatDate
import com.fintrack.mobile.ui.viewmodel.RecordsViewModel

@Composable
fun RecordsScreen(
    currencyCode: String,
    viewModel: RecordsViewModel
) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = stringResource(R.string.records_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.records_tab_history)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.records_tab_stats)) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (selectedTab == 0) {
                HistoryTab(purchases, currencyCode)
            } else {
                StatsTab(purchases, currencyCode)
            }
        }
    }
}

@Composable
private fun HistoryTab(purchases: List<PurchaseEntity>, currencyCode: String) {
    if (purchases.isEmpty()) {
        Text(text = stringResource(R.string.records_empty))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(purchases) { purchase ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = purchase.supermarketName, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = stringResource(R.string.home_purchase_date, formatDate(purchase.dateMillis)))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = formatCurrency(purchase.totalCents, currencyCode))
                }
            }
        }
    }
}

@Composable
private fun StatsTab(purchases: List<PurchaseEntity>, currencyCode: String) {
    val total = purchases.sumOf { it.totalCents }
    val average = if (purchases.isNotEmpty()) total / purchases.size else 0L

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            title = R.string.records_stats_total,
            value = formatCurrency(total, currencyCode)
        )
        StatCard(
            title = R.string.records_stats_avg,
            value = formatCurrency(average, currencyCode)
        )
        StatCard(
            title = R.string.records_stats_count,
            value = purchases.size.toString()
        )
    }
}

@Composable
private fun StatCard(title: Int, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = stringResource(title), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
