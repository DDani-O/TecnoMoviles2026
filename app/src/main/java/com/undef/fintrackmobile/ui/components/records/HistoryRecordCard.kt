package com.undef.fintrackmobile.ui.components.records

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.data.local.entity.ProductEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseWithProducts
import com.undef.fintrackmobile.ui.theme.FintrackBrandColors
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.util.formatCurrency
import com.undef.fintrackmobile.ui.util.formatDate
import java.text.DateFormat
import java.util.*

/**
 * HistoryRecordCard: Tarjeta que representa una compra individual con detalles expandibles.
 * Rediseñada con fondo blanco y bordes definidos para mejor contraste.
 */
@Composable
fun HistoryRecordCard(
    purchase: PurchaseWithProducts,
    currencyCode: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FintrackTheme.colors
    val context = LocalContext.current
    var expanded by rememberSaveable(purchase.purchase.id) { mutableStateOf(value = false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val displayReason = purchase.purchase.reason.ifBlank { stringResource(inferReasonRes(purchase)) }
    
    val shareTitle = stringResource(R.string.share_purchase_title)
    
    /**
     * sharePurchaseDetails: Crea un Intent implícito para compartir los datos de la compra.
     */
    fun sharePurchaseDetails(purchase: PurchaseWithProducts) {
        val shareText = buildString {
            appendLine("🛒 Compra en ${purchase.purchase.supermarketName}")
            appendLine("📅 ${formatDate(purchase.purchase.dateMillis)}")
            appendLine("💰 Total: ${formatCurrency(purchase.purchase.totalCents, currencyCode)}")
            if (purchase.products.isNotEmpty()) {
                appendLine("📦 Incluye ${purchase.products.size} productos")
            }
            appendLine("Registrado con SUPER AHORRO 🏷️")
        }
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Detalle de Compra")
        }
        context.startActivity(Intent.createChooser(intent, shareTitle))
    }
    
    // Cálculos basados en los datos reales de la compra
    val subtotalCents = purchase.products.sumOf { it.priceCents * it.quantity.toLong() }
    val totalFinalCents = purchase.purchase.totalCents
    val discountCents = (subtotalCents - totalFinalCents).coerceAtLeast(0)
    
    val accent = getSupermarketPastelColor(purchase.purchase.supermarketName, colors)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = colors.celesteDeep.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp),
            ),
        colors = CardDefaults.cardColors(containerColor = colors.neutralWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Cabecera: Supermercado, fecha y acciones
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

                    // Chip de Motivo
                    Surface(
                        color = colors.pastelGreenPale,
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
                                tint = colors.pastelGreenDeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.records_label_reason, displayReason),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.pastelGreenDeep
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.records_action_more),
                            tint = subtitleColor
                        )
                    }
                    /**
                     * DropdownMenu: Menú contextual para Editar, Compartir y Eliminar.
                     */
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.records_action_edit)) },
                            onClick = { 
                                menuExpanded = false
                                onEdit() 
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_share)) },
                            onClick = { 
                                menuExpanded = false
                                sharePurchaseDetails(purchase) 
                            },
                            leadingIcon = { Icon(Icons.Default.Share, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.records_action_delete), color = colors.pastelRed) },
                            onClick = { 
                                menuExpanded = false
                                onDelete() 
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = colors.pastelRed) }
                        )
                    }
                }
            }

            // Pie de tarjeta con total y toggle de detalles
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
                        color = colors.celesteDeep
                    )
                }

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
                        color = colors.celesteDeep
                    )
                }
            }

            if (expanded) {
                PurchaseDetailSection(
                    products = purchase.products,
                    currencyCode = currencyCode,
                    subtotalCents = subtotalCents,
                    discountCents = discountCents,
                    totalFinalCents = totalFinalCents
                )
            }
        }
    }
}

@Composable
private fun PurchaseDetailSection(
    products: List<ProductEntity>,
    currencyCode: String,
    subtotalCents: Long,
    discountCents: Long,
    totalFinalCents: Long,
) {
    val colors = FintrackTheme.colors
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
        BreakdownRow(R.string.records_breakdown_subtotal, formatCurrency(subtotalCents, currencyCode))
        if (discountCents > 0) {
            BreakdownRow(R.string.records_breakdown_discount, "- ${formatCurrency(discountCents, currencyCode)}")
        }
        HorizontalDivider(color = colors.celesteSoft.copy(alpha = 0.4f))
        BreakdownRow(
            label = R.string.records_breakdown_total,
            value = formatCurrency(totalFinalCents, currencyCode),
            emphasized = true
        )
    }
}

@Composable
private fun ProductDetailRow(product: ProductEntity, currencyCode: String) {
    val colors = FintrackTheme.colors
    val lineTotal = product.priceCents * product.quantity.toLong()
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.celesteMist),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = formatCurrency(lineTotal, currencyCode), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            Text(
                text = stringResource(R.string.records_product_code, product.id),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.records_product_qty, product.quantity), style = MaterialTheme.typography.bodySmall)
                Text(
                    text = stringResource(R.string.records_product_unit_price, formatCurrency(product.priceCents, currencyCode)),
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

private fun formatTime(dateMillis: Long): String {
    val locale = Locale.Builder().setLanguage("es").setRegion("AR").build()
    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
    return formatter.format(Date(dateMillis))
}

private fun inferReasonRes(purchase: PurchaseWithProducts): Int {
    val items = purchase.products.sumOf { it.quantity }
    return when {
        items >= 12 -> R.string.purchase_tag_large
        items >= 6 -> R.string.purchase_tag_monthly
        purchase.purchase.totalCents >= 50000 -> R.string.purchase_tag_supply
        else -> R.string.purchase_tag_quick
    }
}

private fun getSupermarketPastelColor(name: String, colors: FintrackBrandColors): androidx.compose.ui.graphics.Color {
    return when {
        name.contains("Carrefour", ignoreCase = true) -> colors.pastelBlue
        name.contains("Coto", ignoreCase = true) -> colors.pastelRed
        name.contains("Jumbo", ignoreCase = true) -> colors.pastelGreen
        else -> colors.celesteSoft
    }
}
