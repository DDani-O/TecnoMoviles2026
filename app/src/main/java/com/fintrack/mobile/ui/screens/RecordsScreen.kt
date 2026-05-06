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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
    
    // State for specific month selection (when MONTH is selected)
    val calendar = java.util.Calendar.getInstance()
    var selectedMonthIndex by rememberSaveable { mutableIntStateOf(calendar.get(java.util.Calendar.MONTH)) }
    
    // State for specific year selection (when MONTH or YEAR is selected)
    var selectedYear by rememberSaveable { mutableIntStateOf(calendar.get(java.util.Calendar.YEAR)) }

    val now = System.currentTimeMillis()
    
    val filteredPurchases = remember(purchases, selectedPeriod, selectedMonthIndex, selectedYear) {
        when (selectedPeriod) {
            PeriodFilter.WEEK -> {
                val range = 7L * 24 * 60 * 60 * 1000
                purchases.filter { it.dateMillis >= now - range }
            }
            PeriodFilter.MONTH -> {
                purchases.filter { purchase ->
                    val pCal = java.util.Calendar.getInstance().apply { timeInMillis = purchase.dateMillis }
                    pCal.get(java.util.Calendar.MONTH) == selectedMonthIndex && 
                    pCal.get(java.util.Calendar.YEAR) == selectedYear
                }
            }
            PeriodFilter.YEAR -> {
                purchases.filter { purchase ->
                    val pCal = java.util.Calendar.getInstance().apply { timeInMillis = purchase.dateMillis }
                    pCal.get(java.util.Calendar.YEAR) == selectedYear
                }
            }
        }
    }
    
    val filteredWithProducts = remember(purchasesWithProducts, filteredPurchases) {
        val ids = filteredPurchases.map { it.id }.toSet()
        purchasesWithProducts.filter { it.purchase.id in ids }
    }
    
    val totalSpent = filteredPurchases.sumOf { it.totalCents }
    val average = if (filteredPurchases.isNotEmpty()) totalSpent / filteredPurchases.size else 0L

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecordsSectionHeader(
                title = R.string.records_stats_period_title, 
                subtitle = "Selecciona el tiempo que quieres analizar"
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                PeriodFilterRow(selectedPeriod = selectedPeriod, onSelected = { selectedPeriod = it })
                
                // Show selectors based on the main period chosen
                if (selectedPeriod == PeriodFilter.MONTH) {
                    SpecificMonthSelector(
                        selectedMonth = selectedMonthIndex,
                        selectedYear = selectedYear,
                        onMonthYearSelected = { month, year ->
                            selectedMonthIndex = month
                            selectedYear = year
                        }
                    )
                } else if (selectedPeriod == PeriodFilter.YEAR) {
                    SpecificYearSelector(
                        selectedYear = selectedYear,
                        onYearSelected = { selectedYear = it }
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CelestePale),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
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
                    color = CelesteInk
                )
                Text(
                    text = formatCurrency(totalSpent, currencyCode),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp
                    ),
                    color = CelesteDeep
                )
                HorizontalDivider(color = CelesteSoft.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatCurrency(average, currencyCode),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = CelesteInk
                        )
                        Text(
                            text = "Promedio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CelesteInk.copy(alpha = 0.7f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = filteredPurchases.size.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = CelesteInk
                        )
                        Text(
                            text = "Tickets",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CelesteInk.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        RecordsSectionHeader(
            title = R.string.records_stats_distribution_title, 
            subtitle = "Dónde gastaste más en este periodo"
        )
        SupermarketPieChartCard(stats = filteredPurchases, currencyCode = currencyCode)

        RecordsSectionHeader(
            title = R.string.records_stats_trend_title, 
            subtitle = "Comparativa de gastos mes a mes"
        )
        MonthlyTrendBarCard(purchases = purchases, currencyCode = currencyCode)

        RecordsSectionHeader(
            title = R.string.records_stats_ranking_title, 
            subtitle = "Los productos que más compraste"
        )
        ProductPodiumCard(purchases = filteredWithProducts, currencyCode = currencyCode)
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PeriodFilterRow(
    selectedPeriod: PeriodFilter,
    onSelected: (PeriodFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PeriodFilterButton(
            label = "Semana",
            selected = selectedPeriod == PeriodFilter.WEEK,
            onClick = { onSelected(PeriodFilter.WEEK) }
        )
        PeriodFilterButton(
            label = "Mes",
            selected = selectedPeriod == PeriodFilter.MONTH,
            onClick = { onSelected(PeriodFilter.MONTH) }
        )
        PeriodFilterButton(
            label = "Año",
            selected = selectedPeriod == PeriodFilter.YEAR,
            onClick = { onSelected(PeriodFilter.YEAR) }
        )
    }
}

@Composable
private fun SpecificMonthSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthYearSelected: (Int, Int) -> Unit
) {
    val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(CelestePale.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Año anterior", tint = CelesteDeep)
            }
            Text(
                text = selectedYear.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CelesteDeep
            )
            IconButton(onClick = { onMonthYearSelected(selectedMonth, selectedYear + 1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Año siguiente", tint = CelesteDeep)
            }
        }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(months) { index, month ->
                val isSelected = index == selectedMonth
                Surface(
                    onClick = { onMonthYearSelected(index, selectedYear) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) CelesteDeep else Color.White,
                    modifier = Modifier.width(60.dp)
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = month,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) Color.White else CelesteInk,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecificYearSelector(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(CelestePale.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onYearSelected(selectedYear - 1) }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Año anterior", tint = CelesteDeep)
        }
        Text(
            text = "Año $selectedYear",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CelesteDeep
        )
        IconButton(onClick = { onYearSelected(selectedYear + 1) }) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Año siguiente", tint = CelesteDeep)
        }
    }
}

@Composable
private fun PeriodFilterButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(44.dp)
            .padding(horizontal = 2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) CelesteDeep else CelestePale,
            contentColor = if (selected) Color.White else CelesteInk
        ),
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        )
    }
}

@Composable
private fun SupermarketPieChartCard(
    stats: List<PurchaseEntity>,
    currencyCode: String,
) {
    val grouped = stats.groupBy { it.supermarketName }
        .mapValues { entry -> entry.value.sumOf { it.totalCents } }
        .toList()
        .sortedByDescending { it.second }
    
    val totalSpent = grouped.sumOf { it.second }.toFloat().coerceAtLeast(1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = CelesteMist),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    grouped.forEach { (name, amount) ->
                        val sweepAngle = (amount.toFloat() / totalSpent) * 360f
                        drawArc(
                            color = pastelForSupermarket(name),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grouped.take(4).forEach { (name, amount) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(pastelForSupermarket(name))
                        )
                        Column {
                            Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = formatCurrency(amount, currencyCode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyTrendBarCard(
    purchases: List<PurchaseEntity>,
    currencyCode: String,
) {
    val now = System.currentTimeMillis()
    val monthMillis = 30L * 24 * 60 * 60 * 1000
    val monthData = (0..4).map { index ->
        val end = now - index * monthMillis
        val start = end - monthMillis
        val amount = purchases.filter { it.dateMillis in start until end }.sumOf { it.totalCents }
        // Format month name (simple version)
        val date = java.util.Date(start)
        val label = java.text.SimpleDateFormat("MMM", java.util.Locale("es", "AR")).format(date)
        label to amount
    }.reversed()

    val maxVal = monthData.maxOfOrNull { it.second }?.toFloat()?.coerceAtLeast(1f) ?: 1f

    Card(
        colors = CardDefaults.cardColors(containerColor = CelesteMist),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                monthData.forEach { (label, amount) ->
                    val ratio = amount.toFloat() / maxVal
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (amount > 0) formatCurrency(amount, currencyCode).replace("$", "").take(4) else "",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height((ratio * 120).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(CelesteSoft)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductPodiumCard(
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
        .take(5)

    if (grouped.isEmpty()) return

    Card(
        colors = CardDefaults.cardColors(containerColor = CelestePale),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place
                if (grouped.size >= 2) {
                    PodiumStep(rank = grouped[1], position = 2, height = 80.dp, color = Color(0xFFC0C0C0))
                }
                // 1st Place
                if (grouped.size >= 1) {
                    PodiumStep(rank = grouped[0], position = 1, height = 120.dp, color = Color(0xFFFFD700))
                }
                // 3rd Place
                if (grouped.size >= 3) {
                    PodiumStep(rank = grouped[2], position = 3, height = 60.dp, color = Color(0xFFCD7F32))
                }
            }
            
            if (grouped.size > 3) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    grouped.drop(3).forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.5f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "${index + 4}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(text = "x${item.quantity}", fontWeight = FontWeight.Bold, color = CelesteDeep)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumStep(rank: ProductRank, position: Int, height: androidx.compose.ui.unit.Dp, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = rank.name,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(80.dp)
        )
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position.toString(),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Text(text = "x${rank.quantity}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecordsSectionHeader(title: Any, subtitle: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
        val titleText = when (title) {
            is Int -> stringResource(title)
            is String -> title
            else -> ""
        }
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Bold,
            color = CelesteInk
        )
        subtitle?.let {
            Text(
                text = it,
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
