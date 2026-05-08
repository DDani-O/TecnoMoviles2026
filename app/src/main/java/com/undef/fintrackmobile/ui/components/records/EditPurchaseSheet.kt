package com.undef.fintrackmobile.ui.components.records

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.undef.fintrackmobile.data.local.entity.PurchaseEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseWithProducts
import com.undef.fintrackmobile.ui.components.*
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.util.*
import com.undef.fintrackmobile.ui.viewmodel.EditableProductDraft
import kotlinx.coroutines.launch
import java.util.*

/**
 * EditPurchaseSheet: Hoja modal para modificar los datos de una compra y sus productos.
 * Reutiliza componentes de PurchaseDraftComponents para consistencia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPurchaseSheet(
    purchase: PurchaseWithProducts,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (PurchaseEntity, List<ProductEntity>) -> Unit,
) {
    val context = LocalContext.current
    val colors = FintrackTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    var supermarket by rememberSaveable(purchase.purchase.id) { mutableStateOf(purchase.purchase.supermarketName) }
    var reason by rememberSaveable(purchase.purchase.id) { mutableStateOf(purchase.purchase.reason) }
    var dateMillis by rememberSaveable(purchase.purchase.id) { mutableLongStateOf(purchase.purchase.dateMillis) }

    val productStates = remember(purchase) {
        mutableStateListOf<EditableProductDraft>().apply {
            purchase.products.forEach { product ->
                add(
                    EditableProductDraft(
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

    val totals = calculatePurchaseTotals(productStates)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.celesteMist
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.records_edit_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.celesteDeep
            )

            PurchaseDataSection(
                title = stringResource(R.string.purchase_data_title),
                supermarket = supermarket,
                onSupermarketChange = { supermarket = it },
                reason = reason,
                onReasonChange = { reason = it },
                dateText = formatDate(dateMillis),
                timeText = formatTime(dateMillis),
                onDateClick = { showDatePicker(context, dateMillis) { dateMillis = it } },
                onTimeClick = { showTimePicker(context, dateMillis) { dateMillis = it } }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackSectionHeader(title = R.string.records_products_title)
                
                Button(
                    onClick = {
                        productStates.add(0, EditableProductDraft(purchaseId = purchase.purchase.id, quantity = "1", price = "0.00", discount = "0.00"))
                        scope.launch { scrollState.animateScrollTo(0) }
                    },
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.celesteBase)
                ) {
                    Text("+ Agregar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            productStates.forEachIndexed { index, draft ->
                EditableProductCard(
                    index = index,
                    product = draft,
                    onUpdate = { productStates[index] = it },
                    onRemove = { productStates.removeAt(index) }
                )
            }

            PurchaseBreakdownCard(totals = totals, currencyCode = currencyCode)

            Button(
                onClick = {
                    val updatedProducts = productStates.map { draft ->
                        ProductEntity(
                            id = draft.id,
                            purchaseId = draft.purchaseId,
                            name = draft.name.ifBlank { "Producto" },
                            code = draft.code,
                            description = draft.description,
                            quantity = draft.quantity.toIntOrNull() ?: 0,
                            priceCents = parseCents(draft.price),
                            discountCents = parseCents(draft.discount)
                        )
                    }
                    val updatedPurchase = purchase.purchase.copy(
                        supermarketName = supermarket,
                        reason = reason,
                        dateMillis = dateMillis,
                        totalCents = totals.totalCents
                    )
                    onSave(updatedPurchase, updatedProducts)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = colors.celesteBase)
            ) {
                Text(
                    text = stringResource(R.string.records_action_save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun formatRawPrice(cents: Long): String {
    return String.format(Locale.US, "%.2f", cents / 100.0)
}
