package com.fintrack.mobile.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.theme.FintrackTheme
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.util.parseCents
import com.fintrack.mobile.ui.viewmodel.EditableProductDraft

/**
 * PurchaseTotals: Representa el desglose financiero de una compra.
 */
data class PurchaseTotals(
    val subtotalCents: Long,
    val discountCents: Long,
    val taxesCents: Long,
    val totalCents: Long,
)

/**
 * calculatePurchaseTotals: Función de utilidad para calcular subtotales y totales a partir de borradores de productos.
 */
fun calculatePurchaseTotals(products: List<EditableProductDraft>): PurchaseTotals {
    val subtotalCents = products.sumOf { product ->
        val qty = product.quantity.toIntOrNull()?.coerceAtLeast(0) ?: 0
        parseCents(product.price).coerceAtLeast(0) * qty.toLong()
    }
    val discountCents = products.sumOf { product ->
        val qty = product.quantity.toIntOrNull()?.coerceAtLeast(0) ?: 0
        parseCents(product.discount).coerceAtLeast(0) * qty.toLong()
    }.coerceAtLeast(0)
    val taxableCents = (subtotalCents - discountCents).coerceAtLeast(0)
    val taxesCents = (taxableCents * 0.21f).toLong()
    val totalCents = taxableCents + taxesCents

    return PurchaseTotals(
        subtotalCents = subtotalCents,
        discountCents = discountCents,
        taxesCents = taxesCents,
        totalCents = totalCents
    )
}

/**
 * PurchaseDataSection: Sección de formulario para editar los datos generales de la compra (Supermercado, motivo, fecha).
 */
@Composable
fun PurchaseDataSection(
    title: String,
    supermarket: String,
    onSupermarketChange: (String) -> Unit,
    reason: String,
    onReasonChange: (String) -> Unit,
    dateText: String,
    timeText: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FintrackTheme.colors

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.neutralWhite),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.celesteSoft, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = supermarket,
                    onValueChange = onSupermarketChange,
                    label = { Text(stringResource(R.string.label_supermarket)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text(stringResource(R.string.label_purchase_reason)) },
                    placeholder = { Text(stringResource(R.string.placeholder_purchase_reason)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.label_date)) },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.label_time)) },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.action_change_date),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.celesteDeep,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                            .noRippleClickable(onDateClick)
                    )
                    Text(
                        text = stringResource(R.string.action_change_time),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.celesteDeep,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                            .noRippleClickable(onTimeClick)
                    )
                }
            }
        }
    }
}

/**
 * EditableProductCard: Tarjeta expandible para editar los detalles de un producto (nombre, cantidad, precio, descuento).
 */
@Composable
fun EditableProductCard(
    index: Int,
    product: EditableProductDraft,
    onUpdate: (EditableProductDraft) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FintrackTheme.colors

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.neutralWhite),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.celesteSoft, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.purchase_product_title, index + 1),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.celesteDeep
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.action_delete_product),
                        tint = colors.pastelRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = product.id.toString(),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.label_product_id)) },
                    modifier = Modifier.weight(0.4f),
                    readOnly = true,
                    singleLine = true
                )
                OutlinedTextField(
                    value = product.code,
                    onValueChange = { onUpdate(product.copy(code = it)) },
                    label = { Text(stringResource(R.string.label_product_code_optional)) },
                    modifier = Modifier.weight(0.6f),
                    singleLine = true
                )
            }
            OutlinedTextField(
                value = product.name,
                onValueChange = { onUpdate(product.copy(name = it)) },
                label = { Text(stringResource(R.string.records_product_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = product.description,
                onValueChange = { onUpdate(product.copy(description = it)) },
                label = { Text(stringResource(R.string.label_product_description_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = product.quantity,
                    onValueChange = { onUpdate(product.copy(quantity = it)) },
                    label = { Text(stringResource(R.string.label_product_quantity)) },
                    modifier = Modifier.weight(0.33f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = product.price,
                    onValueChange = { onUpdate(product.copy(price = it)) },
                    label = { Text(stringResource(R.string.label_product_price)) },
                    modifier = Modifier.weight(0.34f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = product.discount,
                    onValueChange = { onUpdate(product.copy(discount = it)) },
                    label = { Text(stringResource(R.string.label_product_discount)) },
                    modifier = Modifier.weight(0.33f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
    }
}

/**
 * PurchaseBreakdownCard: Tarjeta que resume los totales de la compra (subtotal, descuentos, impuestos y total final).
 */
@Composable
fun PurchaseBreakdownCard(
    totals: PurchaseTotals,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    val colors = FintrackTheme.colors

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.celestePale),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.records_breakdown_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            BreakdownRow(
                label = R.string.records_breakdown_subtotal,
                value = formatCurrency(totals.subtotalCents, currencyCode)
            )
            BreakdownRow(
                label = R.string.purchase_breakdown_discount,
                value = "- ${formatCurrency(totals.discountCents, currencyCode)}"
            )
            BreakdownRow(
                label = R.string.records_breakdown_taxes,
                value = formatCurrency(totals.taxesCents, currencyCode)
            )
            HorizontalDivider(color = colors.celesteSoft.copy(alpha = 0.4f))
            BreakdownRow(
                label = R.string.records_breakdown_total,
                value = formatCurrency(totals.totalCents, currencyCode),
                emphasized = true
            )
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

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    return clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) { onClick() }
}
