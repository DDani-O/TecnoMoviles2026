package com.undef.fintrackmobile.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.data.local.entity.PurchaseEntity
import com.undef.fintrackmobile.ui.components.*
import com.undef.fintrackmobile.ui.components.purchase.*
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.util.*
import com.undef.fintrackmobile.ui.viewmodel.PurchaseViewModel
import com.undef.fintrackmobile.ui.viewmodel.SincronizacionEstado
import kotlinx.coroutines.launch
import java.io.File

/**
 * NewPurchaseScreen: Pantalla para el registro de una nueva compra.
 * Permite la carga manual de productos o la extracción mediante foto del ticket.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPurchaseScreen(
    currencyCode: String,
    viewModel: PurchaseViewModel,
    onAdjustTicket: () -> Unit,
) {
    val context = LocalContext.current
    val supermarket by viewModel.supermarket.collectAsStateWithLifecycle()
    val reason by viewModel.reason.collectAsStateWithLifecycle()
    val dateMillis by viewModel.dateMillis.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val synchronizationState by viewModel.estadoSincronizacion.collectAsStateWithLifecycle()
    val colors = FintrackTheme.colors

    val totals = calculatePurchaseTotals(products)
    val showTicketSheetState = rememberSaveable { mutableStateOf(value = false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val syncSuccessMsg = stringResource(R.string.sync_success)
    val syncErrorMsg = stringResource(R.string.sync_error)

    val unknownErrorMsg = stringResource(R.string.unknown_error)

    /**
     * LaunchedEffect: Monitorea el estado de sincronización y muestra el feedback visual correspondiente.
     */
    LaunchedEffect(synchronizationState) {
        when (val state = synchronizationState) {
            is SincronizacionEstado.Exito -> {
                snackbarHostState.showSnackbar(
                    message = syncSuccessMsg,
                    duration = SnackbarDuration.Short
                )
                viewModel.resetSyncStatus()
            }
            is SincronizacionEstado.Error -> {
                val errorText = if (state.mensaje == "UNKNOWN_ERROR") unknownErrorMsg else state.mensaje
                snackbarHostState.showSnackbar(
                    message = syncErrorMsg.format(errorText),
                    duration = SnackbarDuration.Long
                )
                viewModel.resetSyncStatus()
            }
            else -> Unit
        }
    }

    // Gestión de Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            viewModel.setTicketUri(it.toString())
            onAdjustTicket()
        }
    }

    // Gestión de Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        if (success && (uri != null)) {
            viewModel.setTicketUri(uri.toString())
            onAdjustTicket()
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

    Box(modifier = Modifier.fillMaxSize().background(colors.celesteMist)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item(key = "purchase_header") {
                Text(
                    text = stringResource(R.string.purchase_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.celesteDeep,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item(key = "purchase_data") {
                PurchaseDataSection(
                    title = stringResource(R.string.purchase_data_title),
                    supermarket = supermarket,
                    onSupermarketChange = viewModel::setSupermarket,
                    reason = reason,
                    onReasonChange = viewModel::setReason,
                    dateText = formatDate(dateMillis),
                    timeText = formatTime(dateMillis),
                    onDateClick = { showDatePicker(context, dateMillis, viewModel::setDateMillis) },
                    onTimeClick = { showTimePicker(context, dateMillis, viewModel::setDateMillis) },
                )
            }

            item(key = "purchase_actions") {
                PurchaseActionButtons(
                    onAddProduct = {
                        viewModel.addEmptyProduct()
                        scope.launch { listState.animateScrollToItem(3) }
                    },
                    onLoadTicket = { showTicketSheetState.value = true }
                )
            }

            item(key = "products_title") {
                FintrackSectionHeader(
                    title = R.string.records_products_title,
                    subtitle = if (products.isEmpty()) stringResource(R.string.purchase_products_empty) else null
                )
            }

            itemsIndexed(products, key = { _, product -> product.id }) { index, product ->
                EditableProductCard(
                    index = index,
                    product = product,
                    onUpdate = { updated -> viewModel.updateProduct(index, updated) },
                    onRemove = { viewModel.removeProduct(index) }
                )
            }

            item(key = "purchase_totals") {
                PurchaseBreakdownCard(totals = totals, currencyCode = currencyCode)
            }

            item(key = "save_action") {
                val saveSuccessMsg = stringResource(R.string.purchase_save_success)
                val errorNoProducts = stringResource(R.string.purchase_error_no_products)
                val errorNoSupermarket = stringResource(R.string.purchase_error_no_supermarket)
                val actionSyncLabel = stringResource(R.string.action_sync)

                Button(
                    onClick = {
                        if (supermarket.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar(errorNoSupermarket) }
                        } else if (products.isEmpty() || totals.totalCents <= 0L) {
                            scope.launch { snackbarHostState.showSnackbar(errorNoProducts) }
                        } else {
                            /**
                             * Capturamos los datos actuales para la sincronización antes de limpiar el formulario.
                             */
                            val currentPurchase = PurchaseEntity(
                                supermarketName = supermarket,
                                dateMillis = dateMillis,
                                totalCents = totals.totalCents,
                                reason = reason
                            )

                            // Guardado local
                            viewModel.savePurchase(totals.totalCents)
                            
                            // Mostrar Snackbar con acción de Sincronizar (Networking POST)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = saveSuccessMsg,
                                    actionLabel = actionSyncLabel,
                                    duration = SnackbarDuration.Long
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.syncPurchase(currentPurchase)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.celesteBase)
                ) {
                    Text(
                        text = stringResource(R.string.purchase_save),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
    }
    if (showTicketSheetState.value) {
        PurchaseTicketSheet(
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
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
