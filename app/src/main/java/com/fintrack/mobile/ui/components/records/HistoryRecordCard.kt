package com.fintrack.mobile.ui.components.records

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fintrack.mobile.R
import com.fintrack.mobile.data.local.entity.ProductEntity
import com.fintrack.mobile.data.local.entity.PurchaseWithProducts
import com.fintrack.mobile.ui.theme.FintrackBrandColors
import com.fintrack.mobile.ui.theme.FintrackTheme
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.util.formatDate
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
    var expanded by rememberSaveable(purchase.purchase.id) { mutableStateOf(value = false) }
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val displayReason = purchase.purchase.reason.ifBlank { inferReason(purchase) }
    
    // Cálculos (Podrían estar en un UseCase o ViewModel, pero para UI se mantienen aquí)
    val subtotalCents = purchase.products.sumOf { it.priceCents * it.quantity.toLong() }
    val discountCents = (subtotalCents * 0.06f).toLong() // Descuento estático para visualización
    val taxesCents = ((subtotalCents - discountCents) * 0.21f).toLong()
    val totalFinalCents = (subtotalCents - discountCents) + taxesCents
    
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

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, stringResource(R.string.records_action_edit), tint = colors.celesteInk)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, stringResource(R.string.records_action_delete), tint = colors.pastelRed)
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
                    taxesCents = taxesCents,
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
    taxesCents: Long,
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
        BreakdownRow(R.string.records_breakdown_discount, "- ${formatCurrency(discountCents, currencyCode)}")
        BreakdownRow(R.string.records_breakdown_taxes, formatCurrency(taxesCents, currencyCode))
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

private fun inferReason(purchase: PurchaseWithProducts): String {
    val items = purchase.products.sumOf { it.quantity }
    return when {
        items >= 12 -> "Compra grande"
        items >= 6 -> "Compra mensual"
        purchase.purchase.totalCents >= 50000 -> "Compra de abastecimiento"
        else -> "Compra rápida"
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
