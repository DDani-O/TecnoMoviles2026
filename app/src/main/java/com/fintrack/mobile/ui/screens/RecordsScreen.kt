package com.fintrack.mobile.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border 
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.fintrack.mobile.ui.viewmodel.PeriodFilter
import com.fintrack.mobile.ui.viewmodel.ProductRank
import com.fintrack.mobile.ui.viewmodel.RecordsUiState
import com.fintrack.mobile.ui.viewmodel.RecordsViewModel
import com.fintrack.mobile.ui.viewmodel.StatsData
import java.text.DateFormat
import java.util.Date
import java.util.Locale

// Removido PeriodFilter local para usar el del ViewModel

/**
 * RecordsScreen: Pantalla que gestiona el historial de compras y estadísticas detalladas.
 * Utiliza un sistema de pestañas (Tabs) para alternar entre la lista de registros y el análisis gráfico.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    currencyCode: String,
    viewModel: RecordsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Estado local para navegación y diálogos
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var editingPurchase by remember { mutableStateOf<PurchaseWithProducts?>(null) }
    var deleteTarget by remember { mutableStateOf<PurchaseEntity?>(null) }

    val onDismissEdit = remember { { editingPurchase = null } }
    val onDismissDelete = remember { { deleteTarget = null } }

    Surface(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is RecordsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando registros...")
                }
            }
            is RecordsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = Color.Red)
                }
            }
            is RecordsUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.records_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
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
                            purchases = state.purchases,
                            currencyCode = currencyCode,
                            onEdit = { editingPurchase = it },
                            onDelete = { deleteTarget = it.purchase }
                        )
                    } else {
                        StatsTab(
                            statsData = state.filteredStats,
                            currencyCode = currencyCode,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    // Hoja modal para edición de compras.
    editingPurchase?.let { purchase ->
        EditPurchaseSheet(
            purchase = purchase,
            currencyCode = currencyCode,
            onDismiss = onDismissEdit,
            onSave = { updatedPurchase, updatedProducts ->
                viewModel.updatePurchase(updatedPurchase, updatedProducts)
                onDismissEdit()
            }
        )
    }

    // Diálogo de confirmación para eliminación.
    deleteTarget?.let { target ->
        DeleteConfirmationDialog(
            onDismiss = onDismissDelete,
            onConfirm = {
                viewModel.deletePurchase(target)
                onDismissDelete()
            }
        )
    }
}

/**
 * HistoryTab: Muestra una lista desplazable de todos los registros de compra.
 */
@Composable
private fun HistoryTab(
    purchases: List<PurchaseWithProducts>,
    currencyCode: String,
    onEdit: (PurchaseWithProducts) -> Unit,
    onDelete: (PurchaseWithProducts) -> Unit,
) {
    if (purchases.isEmpty()) {
        EmptyHistoryCard()
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

/**
 * HistoryRecordCard: Tarjeta que representa una compra individual con detalles expandibles.
 * Rediseñada con fondo blanco y bordes definidos para mejor contraste.
 */
@Composable
private fun HistoryRecordCard(
    purchase: PurchaseWithProducts,
    currencyCode: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    // Estado local para controlar si el detalle de productos está visible.
    var expanded by rememberSaveable(purchase.purchase.id) { mutableStateOf(false) }
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val reason = remember(purchase) { inferReason(purchase) }
    val subtotalCents = purchase.products.sumOf { it.priceCents * it.quantity.toLong() }
    val discountCents = (subtotalCents * 0.06f).toLong()
    val taxesCents = ((subtotalCents - discountCents) * 0.21f).toLong()
    val totalFinalCents = subtotalCents - discountCents + taxesCents
    val accent = pastelForSupermarket(purchase.purchase.supermarketName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = CelesteDeep.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Cabecera de la tarjeta: Nombre del local y fecha.
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
                    
                    // Etiqueta de "Motivo" o tipo de compra.
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
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
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
                
                // Botones de acción (Editar / Eliminar).
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

            // Botón para expandir/contraer el detalle de productos y monto total visible.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = stringResource(
                            if (expanded) R.string.records_action_hide_detail else R.string.records_action_view_detail
                        ),
                        color = CelesteDeep
                    )
                }

                // Mostramos el monto total directamente en la tarjeta (mejora UI solicitada)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.records_breakdown_total),
                        style = MaterialTheme.typography.labelSmall,
                        color = subtitleColor
                    )
                    Text(
                        text = formatCurrency(totalFinalCents, currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CelesteDeep
                    )
                }
            }

            // Sección de detalle expandible.
            if (expanded) {
                PurchaseDetailSection(
                    products = purchase.products,
                    currencyCode = currencyCode,
                    subtotalCents = subtotalCents,
                    discountCents = discountCents,
                    taxesCents = taxesCents,
                    totalFinalCents = totalFinalCents
                )
            }
        }
    }
}

/**
 * PurchaseDetailSection: Desglose de productos y cálculos finales de una compra.
 */
@Composable
private fun PurchaseDetailSection(
    products: List<ProductEntity>,
    currencyCode: String,
    subtotalCents: Long,
    discountCents: Long,
    taxesCents: Long,
    totalFinalCents: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.records_products_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        products.forEach { product ->
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

/**
 * ProductDetailRow: Fila que muestra la información de un producto individual.
 */
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

/**
 * EmptyHistoryCard: Se muestra cuando no hay compras registradas.
 */
@Composable
private fun EmptyHistoryCard() {
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
}

/**
 * DeleteConfirmationDialog: Diálogo de alerta para confirmar la eliminación de un registro.
 */
@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.records_action_delete)) },
        text = { Text(stringResource(R.string.records_delete_confirm)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.records_action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.records_cancel))
            }
        }
    )
}

/**
 * BreakdownRow: Fila de resumen de costos (subtotal, impuestos, total).
 */
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

/**
 * EditPurchaseSheet: Hoja modal para modificar los datos de una compra y sus productos.
 */
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
    
    // Estado para fecha y hora
    var editDate by rememberSaveable { mutableStateOf(formatDate(purchase.purchase.dateMillis)) }
    var editTime by rememberSaveable { mutableStateOf(formatTime(purchase.purchase.dateMillis)) }
    
    val defaultProductDesc = stringResource(R.string.records_product_desc_placeholder)
    val productStates = remember(purchase) {
        mutableStateListOf<EditableProduct>().apply {
            purchase.products.forEach { product ->
                add(
                    EditableProduct(
                        id = product.id,
                        purchaseId = product.purchaseId,
                        name = product.name,
                        code = product.code,
                        description = product.description,
                        quantity = product.quantity.toString(),
                        price = formatRawPrice(product.priceCents),
                        discount = formatRawPrice(product.discountCents)
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
    val productDiscountCents = productStates.sumOf { editable ->
        parseCents(editable.discount) * (editable.quantity.toIntOrNull() ?: 0).toLong()
    }
    val discountCents = productDiscountCents.coerceAtLeast(0)
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

            // Sección: Datos Generales de la Compra
            Text(
                text = "Datos de la Compra",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CelesteSoft, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = supermarket,
                        onValueChange = { supermarket = it },
                        label = { Text("Supermercado") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = editDate,
                            onValueChange = { editDate = it },
                            label = { Text("Fecha") },
                            modifier = Modifier.weight(1f),
                            readOnly = true
                        )
                        OutlinedTextField(
                            value = editTime,
                            onValueChange = { editTime = it },
                            label = { Text("Hora") },
                            modifier = Modifier.weight(1f),
                            readOnly = true
                        )
                    }
                }
            }

            // Sección: Productos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.records_products_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        productStates.add(
                            EditableProduct(
                                id = 0,
                                purchaseId = purchase.purchase.id,
                                name = "",
                                code = "",
                                description = "",
                                quantity = "1",
                                price = "0.00",
                                discount = "0.00"
                            )
                        )
                    },
                    modifier = Modifier
                        .height(32.dp)
                        .padding(0.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CelesteBase, contentColor = Color.White)
                ) {
                    Text("+ Agregar", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Lista de productos editables
            if (productStates.isEmpty()) {
                Text(
                    text = "No hay productos. Haz clic en '+ Agregar' para añadir uno.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                productStates.forEachIndexed { index, editable ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CelesteSoft, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Encabezado con número de producto y botón de eliminar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Producto ${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CelesteDeep
                                )
                                IconButton(
                                    onClick = { productStates.removeAt(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Eliminar producto",
                                        tint = PastelRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Campos de ID y Código
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = editable.id.toString(),
                                    onValueChange = {},
                                    label = { Text("ID") },
                                    modifier = Modifier.weight(0.4f),
                                    readOnly = true
                                )
                                OutlinedTextField(
                                    value = editable.code,
                                    onValueChange = { productStates[index] = editable.copy(code = it) },
                                    label = { Text("Código (opcional)") },
                                    modifier = Modifier.weight(0.6f)
                                )
                            }

                            // Nombre
                            OutlinedTextField(
                                value = editable.name,
                                onValueChange = { productStates[index] = editable.copy(name = it) },
                                label = { Text("Nombre del producto") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Descripción
                            OutlinedTextField(
                                value = editable.description,
                                onValueChange = { productStates[index] = editable.copy(description = it) },
                                label = { Text("Descripción (opcional)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Cantidad, Precio, Descuento
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = editable.quantity,
                                    onValueChange = { productStates[index] = editable.copy(quantity = it) },
                                    label = { Text("Cant.") },
                                    modifier = Modifier.weight(0.3f)
                                )
                                OutlinedTextField(
                                    value = editable.price,
                                    onValueChange = { productStates[index] = editable.copy(price = it) },
                                    label = { Text("Precio") },
                                    modifier = Modifier.weight(0.35f)
                                )
                                OutlinedTextField(
                                    value = editable.discount,
                                    onValueChange = { productStates[index] = editable.copy(discount = it) },
                                    label = { Text("Desc.") },
                                    modifier = Modifier.weight(0.35f)
                                )
                            }
                        }
                    }
                }
            }

            // Resumen de cálculos
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

            // Botón de guardar
            Button(
                onClick = {
                    val updatedProducts = productStates.map { editable ->
                        val qty = editable.quantity.toIntOrNull() ?: 0
                        val price = parseCents(editable.price)
                        val discount = parseCents(editable.discount)
                        ProductEntity(
                            id = editable.id,
                            purchaseId = editable.purchaseId,
                            name = editable.name.ifBlank { defaultProductDesc },
                            code = editable.code,
                            description = editable.description,
                            quantity = qty,
                            priceCents = price,
                            discountCents = discount
                        )
                    }
                    
                    // Recalcular fecha/hora (por ahora, mantenemos la original)
                    val updatedPurchase = purchase.purchase.copy(
                        supermarketName = supermarket.ifBlank { purchase.purchase.supermarketName },
                        totalCents = totalFinalCents
                    )
                    
                    onSave(updatedPurchase, updatedProducts)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CelesteBase, contentColor = Color.White)
            ) {
                Text(stringResource(R.string.records_action_save))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatsTab(
    statsData: StatsData,
    currencyCode: String,
    viewModel: RecordsViewModel
) {
    var selectedPeriod by rememberSaveable { mutableStateOf(PeriodFilter.MONTH) }
    
    val calendar = java.util.Calendar.getInstance()
    var selectedMonthIndex by rememberSaveable { mutableIntStateOf(calendar.get(java.util.Calendar.MONTH)) }
    var selectedYear by rememberSaveable { mutableIntStateOf(calendar.get(java.util.Calendar.YEAR)) }

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
                PeriodFilterRow(
                    selectedPeriod = selectedPeriod, 
                    onSelected = { 
                        selectedPeriod = it
                        viewModel.setPeriod(it)
                    }
                )
                
                if (selectedPeriod == PeriodFilter.MONTH) {
                    SpecificMonthSelector(
                        selectedMonth = selectedMonthIndex,
                        selectedYear = selectedYear,
                        onMonthYearSelected = { month, year ->
                            selectedMonthIndex = month
                            selectedYear = year
                            viewModel.setMonthYear(month, year)
                        }
                    )
                } else if (selectedPeriod == PeriodFilter.YEAR) {
                    SpecificYearSelector(
                        selectedYear = selectedYear,
                        onYearSelected = { 
                            selectedYear = it
                            viewModel.setMonthYear(selectedMonthIndex, it)
                        }
                    )
                }
            }
        }

        StatsSummaryCard(
            selectedPeriod = selectedPeriod,
            selectedMonthIndex = selectedMonthIndex,
            selectedYear = selectedYear,
            totalSpent = statsData.totalSpentCents,
            average = statsData.averageSpentCents,
            ticketCount = statsData.ticketCount,
            currencyCode = currencyCode
        )

        RecordsSectionHeader(
            title = R.string.records_stats_distribution_title, 
            subtitle = "Dónde gastaste más en este periodo"
        )
        SupermarketPieChartCard(distribution = statsData.supermarketDistribution, currencyCode = currencyCode)

        RecordsSectionHeader(
            title = R.string.records_stats_ranking_title, 
            subtitle = "Los productos que más compraste"
        )
        ProductPodiumCard(ranking = statsData.productRanking)
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * StatsSummaryCard: Muestra el total gastado, promedio y cantidad de tickets del periodo.
 */
@Composable
private fun StatsSummaryCard(
    selectedPeriod: PeriodFilter,
    selectedMonthIndex: Int,
    selectedYear: Int,
    totalSpent: Long,
    average: Long,
    ticketCount: Int,
    currencyCode: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CelestePale),
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = formatCurrency(average, currencyCode), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = CelesteInk)
                    Text(text = "Promedio", style = MaterialTheme.typography.bodyMedium, color = CelesteInk.copy(alpha = 0.7f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = ticketCount.toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = CelesteInk)
                    Text(text = "Tickets", style = MaterialTheme.typography.bodyMedium, color = CelesteInk.copy(alpha = 0.7f))
                }
            }
        }
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
        PeriodFilterButton(label = "Semana", selected = selectedPeriod == PeriodFilter.WEEK, onClick = { onSelected(PeriodFilter.WEEK) })
        PeriodFilterButton(label = "Mes", selected = selectedPeriod == PeriodFilter.MONTH, onClick = { onSelected(PeriodFilter.MONTH) })
        PeriodFilterButton(label = "Año", selected = selectedPeriod == PeriodFilter.YEAR, onClick = { onSelected(PeriodFilter.YEAR) })
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Año anterior", tint = CelesteDeep)
            }
            Text(text = selectedYear.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CelesteDeep)
            IconButton(onClick = { onMonthYearSelected(selectedMonth, selectedYear + 1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Año siguiente", tint = CelesteDeep)
            }
        }
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            itemsIndexed(months) { index, month ->
                val isSelected = index == selectedMonth
                Surface(
                    onClick = { onMonthYearSelected(index, selectedYear) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) CelesteDeep else Color.White,
                    modifier = Modifier.width(60.dp)
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(text = month, style = MaterialTheme.typography.labelLarge, color = if (isSelected) Color.White else CelesteInk, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Año anterior", tint = CelesteDeep)
        }
        Text(text = "Año $selectedYear", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CelesteDeep)
        IconButton(onClick = { onYearSelected(selectedYear + 1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Año siguiente", tint = CelesteDeep)
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
        modifier = Modifier.height(44.dp).padding(horizontal = 2.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (selected) CelesteDeep else CelestePale, contentColor = if (selected) Color.White else CelesteInk),
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
    }
}

@Composable
private fun SupermarketPieChartCard(
    distribution: List<Pair<String, Long>>,
    currencyCode: String,
) {
    val totalSpent = distribution.sumOf { it.second }.toFloat().coerceAtLeast(1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = CelesteMist),
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
                        drawArc(color = pastelForSupermarket(name), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true)
                        startAngle += sweepAngle
                    }
                }
            }

            Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                distribution.take(4).forEach { (name, amount) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(pastelForSupermarket(name)))
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
private fun ProductPodiumCard(
    ranking: List<ProductRank>
) {
    if (ranking.isEmpty()) return

    Card(
        colors = CardDefaults.cardColors(containerColor = CelestePale),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                if (ranking.size >= 2) PodiumStep(rank = ranking[1], position = 2, height = 80.dp, color = Color(0xFFC0C0C0))
                if (ranking.isNotEmpty()) PodiumStep(rank = ranking[0], position = 1, height = 120.dp, color = Color(0xFFFFD700))
                if (ranking.size >= 3) PodiumStep(rank = ranking[2], position = 3, height = 60.dp, color = Color(0xFFCD7F32))
            }
            
            if (ranking.size > 3) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ranking.drop(3).forEachIndexed { index, item ->
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.5f)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
        Text(text = rank.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(80.dp))
        Box(modifier = Modifier.width(70.dp).height(height).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(color), contentAlignment = Alignment.Center) {
            Text(text = position.toString(), style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold), color = Color.White.copy(alpha = 0.8f))
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
        Text(text = titleText, style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp), fontWeight = FontWeight.Bold, color = CelesteInk)
        subtitle?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class EditableProduct(
    val id: Long,
    val purchaseId: Long,
    val name: String,
    val code: String,
    val description: String,
    val quantity: String,
    val price: String,
    val discount: String,
)

// Removido ProductRank local para usar el del ViewModel

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
private val PastelBlue = Color(0xFF7BB9F1)
private val PastelRed = Color(0xFFF3A1A1)
private val PastelGreen = Color(0xFF7BCB85)
