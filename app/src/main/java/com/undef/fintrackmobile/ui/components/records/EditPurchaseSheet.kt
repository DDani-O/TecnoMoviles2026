package com.undef.fintrackmobile.ui.components.records

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
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
import java.io.File

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
    val defaultProductName = stringResource(R.string.product_default_name)
    
    var supermarket by rememberSaveable(purchase.purchase.id) { mutableStateOf(purchase.purchase.supermarketName) }
    var reason by rememberSaveable(purchase.purchase.id) { mutableStateOf(purchase.purchase.reason) }
    var dateMillis by rememberSaveable(purchase.purchase.id) { mutableLongStateOf(purchase.purchase.dateMillis) }
    var ticketImageUrl by rememberSaveable(purchase.purchase.id) { mutableStateOf(purchase.purchase.ticketImageUrl) }

    val showTicketSheetState = rememberSaveable { mutableStateOf(value = false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { ticketImageUrl = it.toString() }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        if (success && (uri != null)) {
            ticketImageUrl = uri.toString()
        }
    }

    val launchCamera = remember(context) {
        {
            val uri = createTicketUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
    }

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

            FintrackSectionHeader(title = R.string.purchase_ticket_title)
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.neutralWhite),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.celesteSoft, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            ) {
                if (ticketImageUrl != null) {
                    AsyncImage(
                        model = ticketImageUrl,
                        contentDescription = "Ticket Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.purchase_ticket_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.celesteInk,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            Button(
                onClick = { showTicketSheetState.value = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = colors.celesteBase)
            ) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.purchase_ticket_load))
            }

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
                    Text(stringResource(R.string.purchase_add_inline), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
                            name = draft.name.ifBlank { defaultProductName },
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
                        totalCents = totals.totalCents,
                        ticketImageUrl = ticketImageUrl
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
    if (showTicketSheetState.value) {
        ImagePickerSheet(
            title = stringResource(R.string.purchase_ticket_sheet_title),
            sheetState = sheetState,
            onDismiss = { showTicketSheetState.value = false },
            onGalleryClick = {
                showTicketSheetState.value = false
                galleryLauncher.launch("image/*")
            },
            onCameraClick = {
                showTicketSheetState.value = false
                val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) launchCamera() else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
        )
    }
}

private fun createTicketUri(context: Context): Uri {
    val ticketsDir = File(context.cacheDir, "tickets").apply { if (!exists()) mkdirs() }
    val file = File(ticketsDir, "ticket_${System.currentTimeMillis()}.jpg")
    return androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun formatRawPrice(cents: Long): String {
    return String.format(Locale.US, "%.2f", cents / 100.0)
}
