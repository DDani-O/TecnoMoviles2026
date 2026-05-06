package com.fintrack.mobile.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.data.local.entity.ProductEntity
import com.fintrack.mobile.data.local.entity.PurchaseEntity
import com.fintrack.mobile.data.local.entity.PurchaseWithProducts
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.util.formatDate
import com.fintrack.mobile.ui.util.parseCents
import com.fintrack.mobile.ui.viewmodel.RecordsViewModel
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private enum class PeriodFilter { WEEK, MONTH, YEAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    currencyCode: String,
    viewModel: RecordsViewModel,
    modifier: Modifier = Modifier,
) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val purchasesWithProducts by viewModel.purchasesWithProducts.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var editingPurchase by remember { mutableStateOf<PurchaseWithProducts?>(null) }
    var deleteTarget by remember { mutableStateOf<PurchaseEntity?>(null) }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = stringResource(R.string.records_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
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
                HistoryTab(
                    purchases = purchasesWithProducts,
                    currencyCode = currencyCode,
                    onEdit = { editingPurchase = it },
                    onDelete = { deleteTarget = it.purchase }
                )
            } else {
                StatsTab(
                    purchases = purchases,
                    purchasesWithProducts = purchasesWithProducts,
                    currencyCode = currencyCode
                )
            }
        }
    }

    if (editingPurchase != null) {
        EditPurchaseSheet(
            purchase = editingPurchase!!,
            currencyCode = currencyCode,
            onDismiss = { editingPurchase = null },
            onSave = { updatedPurchase, updatedProducts ->
                viewModel.updatePurchase(updatedPurchase, updatedProducts)
                editingPurchase = null
            }
        )
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.records_action_delete)) },
            text = { Text(stringResource(R.string.records_delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePurchase(deleteTarget!!)
                        deleteTarget = null
                    }
                ) {
                    Text(stringResource(R.string.records_action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.records_cancel))
                }
            }
        )
    }
}

@Composable
private fun HistoryTab(
    purchases: List<PurchaseWithProducts>,
    currencyCode: String,
    onEdit: (PurchaseWithProducts) -> Unit,
    onDelete: (PurchaseWithProducts) -> Unit,
) {
    if (purchases.isEmpty()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CelesteMist),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(R.string.records_empty),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(purchases, key = { it.purchase.id }) { purchase ->
            HistoryRecordCard(
                purchase = purchase,
                currencyCode = currencyCode,
                onEdit = { onEdit(purchase) },
                onDelete = { onDelete(purchase) }
            )
        }
    }
}

@Composable
private fun HistoryRecordCard(
    purchase: PurchaseWithProducts,
    currencyCode: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by rememberSaveable(purchase.purchase.id) { mutableStateOf(false) }
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val reason = remember(purchase) { inferReason(purchase) }
    val subtotalCents = purchase.products.sumOf { it.priceCents * it.quantity.toLong() }
    val discountCents = (subtotalCents * 0.06f).toLong()
    val taxesCents = ((subtotalCents - discountCents) * 0.21f).toLong()
    val totalFinalCents = subtotalCents - discountCents + taxesCents
    val accent = pastelForSupermarket(purchase.purchase.supermarketName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CelestePale),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = CircleShape, color = accent.copy(alpha = 0.2f)) {
                            Icon(
                                imageVector = Icons.Filled.Storefront,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Text(
                            text = purchase.purchase.supermarketName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = stringResource(R.string.records_label_date, formatDate(purchase.purchase.dateMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = subtitleColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.records_label_time, formatTime(purchase.purchase.dateMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor
                        )
                    }
                    Surface(
                        color = PastelGreenPale,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = PastelGreenDeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.records_label_reason, reason),
                                style = MaterialTheme.typography.labelMedium,
                                color = PastelGreenDeep
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.records_action_edit),
                            tint = CelesteInk
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.records_action_delete),
                            tint = PastelRed
                        )
                    }
                }
            }

            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    text = stringResource(
                        if (expanded) R.string.records_action_hide_detail else R.string.records_action_view_detail
                    ),
                    color = CelesteDeep
                )
            }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.records_products_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    purchase.products.forEach { product ->
                        ProductDetailRow(product = product, currencyCode = currencyCode)
                    }

                    Text(
                        text = stringResource(R.string.records_breakdown_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    BreakdownRow(
                        label = R.string.records_breakdown_subtotal,
                        value = formatCurrency(subtotalCents, currencyCode)
                    )
                    BreakdownRow(
                        label = R.string.records_breakdown_discount,
                        value = "- ${formatCurrency(discountCents, currencyCode)}"
                    )
                    BreakdownRow(
                        label = R.string.records_breakdown_taxes,
                        value = formatCurrency(taxesCents, currencyCode)
                    )
                    HorizontalDivider(color = CelesteSoft.copy(alpha = 0.4f))
                    BreakdownRow(
                        label = R.string.records_breakdown_total,
                        value = formatCurrency(totalFinalCents, currencyCode),
                        emphasized = true
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductDetailRow(product: ProductEntity, currencyCode: String) {
    val lineTotal = product.priceCents * product.quantity.toLong()
    Card(
        colors = CardDefaults.cardColors(containerColor = CelesteMist),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatCurrency(lineTotal, currencyCode),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(R.string.records_product_code, product.id),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.records_product_desc,
                    stringResource(R.string.records_product_desc_placeholder)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.records_product_qty, product.quantity),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(
                        R.string.records_product_unit_price,
                        formatCurrency(product.priceCents, currencyCode)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(@StringRes label: Int, value: String, emphasized: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = stringResource(label),
            style = if (emphasized) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (emphasized) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPurchaseSheet(
    purchase: PurchaseWithProducts,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (PurchaseEntity, List<ProductEntity>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var supermarket by rememberSaveable(purchase.purchase.id) { mutableStateOf(purchase.purchase.supermarketName) }
    val defaultProductDesc = stringResource(R.string.records_product_desc_placeholder)
    val productStates = remember(purchase) {
        mutableStateListOf<EditableProduct>().apply {
            purchase.products.forEach { product ->
                add(
                    EditableProduct(
                        id = product.id,
                        purchaseId = product.purchaseId,
                        name = product.name,
                        quantity = product.quantity.toString(),
                        price = formatRawPrice(product.priceCents)
                    )
                )
            }
        }
    }

    val subtotalCents = productStates.sumOf { editable ->
        val qty = editable.quantity.toIntOrNull() ?: 0
        val price = parseCents(editable.price)
        price * qty.toLong()
    }
    val discountCents = (subtotalCents * 0.06f).toLong()
    val taxesCents = ((subtotalCents - discountCents) * 0.21f).toLong()
    val totalFinalCents = subtotalCents - discountCents + taxesCents

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CelesteMist
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.records_edit_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = supermarket,
                onValueChange = { supermarket = it },
                label = { Text(stringResource(R.string.records_label_store_title)) },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.records_products_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            productStates.forEachIndexed { index, editable ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CelestePale),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editable.name,
                            onValueChange = { productStates[index] = editable.copy(name = it) },
                            label = { Text(stringResource(R.string.records_product_name)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = editable.quantity,
                                onValueChange = { productStates[index] = editable.copy(quantity = it) },
                                label = { Text(stringResource(R.string.records_product_qty_title)) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editable.price,
                                onValueChange = { productStates[index] = editable.copy(price = it) },
                                label = { Text(stringResource(R.string.records_product_unit_price_title)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CelestePale),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BreakdownRow(
                        label = R.string.records_breakdown_subtotal,
                        value = formatCurrency(subtotalCents, currencyCode)
                    )
                    BreakdownRow(
                        label = R.string.records_breakdown_discount,
                        value = "- ${formatCurrency(discountCents, currencyCode)}"
                    )
                    BreakdownRow(
                        label = R.string.records_breakdown_taxes,
                        value = formatCurrency(taxesCents, currencyCode)
                    )
                    HorizontalDivider(color = CelesteSoft.copy(alpha = 0.4f))
                    BreakdownRow(
                        label = R.string.records_breakdown_total,
                        value = formatCurrency(totalFinalCents, currencyCode),
                        emphasized = true
                    )
                }
            }

            Button(
                onClick = {
                    val updatedProducts = productStates.map { editable ->
                        val qty = editable.quantity.toIntOrNull() ?: 0
                        val price = parseCents(editable.price)
                        ProductEntity(
                            id = editable.id,
                            purchaseId = editable.purchaseId,
                            name = editable.name.ifBlank { defaultProductDesc },
                            quantity = qty,
                            priceCents = price
                        )
                    }
                    onSave(
                        purchase.purchase.copy(
                            supermarketName = supermarket.ifBlank { purchase.purchase.supermarketName },
                            totalCents = totalFinalCents
                        ),
                        updatedProducts
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CelesteBase, contentColor = Color.White)
            ) {
                Text(stringResource(R.string.records_action_save))
            }
        }
    }
}

@Composable
private fun StatsTab(
    purchases: List<PurchaseEntity>,
    purchasesWithProducts: List<PurchaseWithProducts>,
    currencyCode: String,
) {
    var selectedPeriod by rememberSaveable { mutableStateOf(PeriodFilter.MONTH) }
    val now = System.currentTimeMillis()
    val rangeMillis = when (selectedPeriod) {
        PeriodFilter.WEEK -> 7L * 24 * 60 * 60 * 1000
        PeriodFilter.MONTH -> 30L * 24 * 60 * 60 * 1000
        PeriodFilter.YEAR -> 365L * 24 * 60 * 60 * 1000
    }
    val filteredPurchases = remember(purchases, selectedPeriod) {
        purchases.filter { it.dateMillis >= now - rangeMillis }
    }
    val filteredWithProducts = remember(purchasesWithProducts, selectedPeriod) {
        purchasesWithProducts.filter { it.purchase.dateMillis >= now - rangeMillis }
    }
    val totalSpent = filteredPurchases.sumOf { it.totalCents }
    val average = if (filteredPurchases.isNotEmpty()) totalSpent / filteredPurchases.size else 0L

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RecordsSectionHeader(title = R.string.records_stats_period_title, subtitle = null)
        PeriodFilterRow(selectedPeriod = selectedPeriod, onSelected = { selectedPeriod = it })

        Card(
            colors = CardDefaults.cardColors(containerColor = CelesteMist),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = formatCurrency(totalSpent, currencyCode),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.records_stats_avg, formatCurrency(average, currencyCode)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.records_stats_count, filteredPurchases.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        RecordsSectionHeader(title = R.string.records_stats_distribution_title, subtitle = null)
        SupermarketDistributionCard(stats = filteredPurchases, currencyCode = currencyCode)

        RecordsSectionHeader(title = R.string.records_stats_trend_title, subtitle = null)
        MonthlyTrendCard(purchases = purchases, currencyCode = currencyCode)

        RecordsSectionHeader(title = R.string.records_stats_ranking_title, subtitle = null)
        ProductRankingCard(purchases = filteredWithProducts, currencyCode = currencyCode)
    }
}

@Composable
private fun PeriodFilterRow(
    selectedPeriod: PeriodFilter,
    onSelected: (PeriodFilter) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PeriodFilterButton(
            label = R.string.records_period_week,
            selected = selectedPeriod == PeriodFilter.WEEK,
            onClick = { onSelected(PeriodFilter.WEEK) }
        )
        PeriodFilterButton(
            label = R.string.records_period_month,
            selected = selectedPeriod == PeriodFilter.MONTH,
            onClick = { onSelected(PeriodFilter.MONTH) }
        )
        PeriodFilterButton(
            label = R.string.records_period_year,
            selected = selectedPeriod == PeriodFilter.YEAR,
            onClick = { onSelected(PeriodFilter.YEAR) }
        )
    }
}

@Composable
private fun PeriodFilterButton(
    @StringRes label: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) CelestePale else Color.Transparent,
            contentColor = if (selected) CelesteDeep else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (selected) CelesteSoft else CelestePale)
        )
    ) {
        Text(text = stringResource(label))
    }
}

@Composable
private fun SupermarketDistributionCard(
    stats: List<PurchaseEntity>,
    currencyCode: String,
) {
    val grouped = stats.groupBy { it.supermarketName }
        .mapValues { entry -> entry.value.sumOf { it.totalCents } }
        .toList()
        .sortedByDescending { it.second }
    val maxSpent = grouped.maxOfOrNull { it.second }?.toFloat()?.coerceAtLeast(1f) ?: 1f

    Card(
        colors = CardDefaults.cardColors(containerColor = CelestePale),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            grouped.forEach { (name, total) ->
                val ratio = total.toFloat() / maxSpent
                val barColor = pastelForSupermarket(name)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = formatCurrency(total, currencyCode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = barColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(barColor.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio)
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyTrendCard(
    purchases: List<PurchaseEntity>,
    currencyCode: String,
) {
    val now = System.currentTimeMillis()
    val monthMillis = 30L * 24 * 60 * 60 * 1000
    val monthTotals = (0..5).map { index ->
        val end = now - index * monthMillis
        val start = end - monthMillis
        purchases.filter { it.dateMillis in start until end }.sumOf { it.totalCents }
    }.reversed()
    val labels = (1..monthTotals.size).map { "M$it" }

    Card(
        colors = CardDefaults.cardColors(containerColor = CelesteMist),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.records_stats_trend_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LineChart(values = monthTotals.map { it.toFloat() }, labels = labels)
            Text(
                text = stringResource(
                    R.string.records_stats_trend_total,
                    formatCurrency(monthTotals.sum(), currencyCode)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = CelesteInk
            )
        }
    }
}

@Composable
private fun LineChart(values: List<Float>, labels: List<String>) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val stepX = if (values.size > 1) size.width / (values.size - 1) else size.width
            val points = values.mapIndexed { index, value ->
                val ratio = value / maxValue
                val x = stepX * index
                val y = size.height - (ratio * size.height)
                x to y
            }
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = CelesteDeep,
                    start = androidx.compose.ui.geometry.Offset(points[i].first, points[i].second),
                    end = androidx.compose.ui.geometry.Offset(points[i + 1].first, points[i + 1].second),
                    strokeWidth = 4f
                )
            }
            points.forEach { (x, y) ->
                drawCircle(
                    color = CelesteBase,
                    radius = 6f,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { label ->
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProductRankingCard(
    purchases: List<PurchaseWithProducts>,
    currencyCode: String,
) {
    val grouped = purchases.flatMap { it.products }
        .groupBy { it.name }
        .map { (name, items) ->
            val qty = items.sumOf { it.quantity }
            val total = items.sumOf { it.priceCents * it.quantity.toLong() }
            ProductRank(name, qty, total)
        }
        .sortedByDescending { it.quantity }
        .take(6)

    val maxQty = grouped.maxOfOrNull { it.quantity }?.toFloat()?.coerceAtLeast(1f) ?: 1f

    Card(
        colors = CardDefaults.cardColors(containerColor = CelestePale),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            grouped.forEach { item ->
                val ratio = item.quantity / maxQty
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = formatCurrency(item.totalCents, currencyCode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(OrangePastel)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio)
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(OfferPeach)
                        )
                    }
                    Text(
                        text = stringResource(R.string.records_product_qty, item.quantity),
                        style = MaterialTheme.typography.labelSmall,
                        color = CelesteInk
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordsSectionHeader(@StringRes title: Int, subtitle: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = stringResource(title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        subtitle?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class EditableProduct(
    val id: Long,
    val purchaseId: Long,
    val name: String,
    val quantity: String,
    val price: String,
)

private data class ProductRank(
    val name: String,
    val quantity: Int,
    val totalCents: Long,
)

private fun formatTime(dateMillis: Long): String {
    val locale = Locale.Builder().setLanguage("es").setRegion("AR").build()
    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
    return formatter.format(Date(dateMillis))
}

private fun inferReason(purchase: PurchaseWithProducts): String {
    val items = purchase.products.sumOf { it.quantity }
    return when {
        items >= 12 -> "Compra grande"
        items >= 6 -> "Compra mensual"
        purchase.purchase.totalCents >= 50000 -> "Compra de abastecimiento"
        else -> "Compra rapida"
    }
}

private fun formatRawPrice(cents: Long): String {
    return String.format(Locale.US, "%.2f", cents / 100.0)
}

private fun pastelForSupermarket(name: String): Color {
    return when {
        name.contains("Carrefour", ignoreCase = true) -> PastelBlue
        name.contains("Coto", ignoreCase = true) -> PastelRed
        name.contains("Jumbo", ignoreCase = true) -> PastelGreen
        else -> CelesteSoft
    }
}

private val CelesteBase = Color(0xFF33B2C3)
private val CelesteSoft = Color(0xFF54BDCA)
private val CelesteDeep = Color(0xFF1E8D9B)
private val CelestePale = Color(0xFFD8F4F7)
private val CelesteMist = Color(0xFFF0FBFC)
private val CelesteInk = Color(0xFF1B4B52)
private val PastelGreenDeep = Color(0xFF5FAF9C)
private val PastelGreenPale = Color(0xFFE6F6F1)
private val OrangePastel = Color(0xFFFFE3C7)
private val OfferPeach = Color(0xFFF1B591)
private val PastelBlue = Color(0xFF7BB9F1)
private val PastelRed = Color(0xFFF3A1A1)
private val PastelGreen = Color(0xFF7BCB85)
