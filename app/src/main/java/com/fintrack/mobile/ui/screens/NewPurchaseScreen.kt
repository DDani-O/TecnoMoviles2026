package com.fintrack.mobile.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.components.EditableProductCard
import com.fintrack.mobile.ui.components.PurchaseBreakdownCard
import com.fintrack.mobile.ui.components.PurchaseDataSection
import com.fintrack.mobile.ui.components.calculatePurchaseTotals
import com.fintrack.mobile.ui.theme.FintrackTheme
import com.fintrack.mobile.ui.util.formatDate
import com.fintrack.mobile.ui.util.formatTime
import com.fintrack.mobile.ui.util.updateDateMillis
import com.fintrack.mobile.ui.util.updateTimeMillis
import com.fintrack.mobile.ui.viewmodel.PurchaseViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

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
    val colors = FintrackTheme.colors

    val totals = calculatePurchaseTotals(products)
    var showTicketSheet by rememberSaveable { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.setTicketUri(uri.toString())
            onAdjustTicket()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        if (success && uri != null) {
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
        if (granted) {
            launchCamera()
        }
    }

    val openDatePicker = remember(context, dateMillis) {
        {
            val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    viewModel.setDateMillis(updateDateMillis(dateMillis, year, month, dayOfMonth))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    val openTimePicker = remember(context, dateMillis) {
        {
            val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    viewModel.setDateMillis(updateTimeMillis(dateMillis, hour, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(context)
            ).show()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.celesteMist
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                // Título unificado con ExploreScreen
                Text(
                    text = stringResource(R.string.purchase_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.celesteDeep
                )
            }
            item {
                PurchaseDataSection(
                    title = stringResource(R.string.purchase_data_title),
                    supermarket = supermarket,
                    onSupermarketChange = viewModel::setSupermarket,
                    reason = reason,
                    onReasonChange = viewModel::setReason,
                    dateText = formatDate(dateMillis),
                    timeText = formatTime(dateMillis),
                    onDateClick = openDatePicker,
                    onTimeClick = openTimePicker,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            viewModel.addEmptyProduct()
                            // Scroll al inicio para ver el nuevo producto inmediatamente
                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.celesteBase,
                            contentColor = colors.neutralWhite
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.purchase_load_product))
                    }
                    OutlinedButton(
                        onClick = { showTicketSheet = true },
                        border = BorderStroke(1.dp, colors.celesteBase),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.purchase_load_ticket), color = colors.celesteBase)
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.records_products_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.celesteInk
                )
            }
            if (products.isEmpty()) {
                item {
                    Text(text = stringResource(R.string.purchase_products_empty))
                }
            } else {
                itemsIndexed(products, key = { _, product -> product.id }) { index, product ->
                    EditableProductCard(
                        index = index,
                        product = product,
                        onUpdate = { updated -> viewModel.updateProduct(index, updated) },
                        onRemove = { viewModel.removeProduct(index) }
                    )
                }
            }
            item {
                PurchaseBreakdownCard(totals = totals, currencyCode = currencyCode)
            }
            item {
                Button(
                    onClick = {
                        if (totals.totalCents > 0L && supermarket.isNotBlank()) {
                            viewModel.savePurchase(totals.totalCents)
                            scope.launch {
                                snackbarHostState.showSnackbar("Compra guardada correctamente")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.celesteBase,
                        contentColor = colors.neutralWhite
                    )
                ) {
                    Text(text = stringResource(R.string.purchase_save))
                }
            }
        }
    }

    if (showTicketSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTicketSheet = false },
            sheetState = sheetState,
            containerColor = colors.celesteMist
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.purchase_ticket_sheet_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        showTicketSheet = false
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.celesteBase,
                        contentColor = colors.neutralWhite
                    )
                ) {
                    Icon(imageVector = Icons.Filled.UploadFile, contentDescription = null)
                    Text(
                        text = stringResource(R.string.purchase_ticket_gallery),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                OutlinedButton(
                    onClick = {
                        showTicketSheet = false
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            launchCamera()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, colors.celesteBase)
                ) {
                    Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null, tint = colors.celesteBase)
                    Text(
                        text = stringResource(R.string.purchase_ticket_camera),
                        color = colors.celesteBase,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

private fun createTicketUri(context: Context): Uri {
    val ticketsDir = File(context.cacheDir, "tickets")
    if (!ticketsDir.exists()) {
        ticketsDir.mkdirs()
    }
    val file = File(ticketsDir, "ticket_${System.currentTimeMillis()}.jpg")
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}
