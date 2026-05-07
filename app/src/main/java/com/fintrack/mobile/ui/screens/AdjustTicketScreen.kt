package com.fintrack.mobile.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import java.util.Calendar

@Composable
fun AdjustTicketScreen(
    currencyCode: String,
    viewModel: PurchaseViewModel,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val supermarket by viewModel.supermarket.collectAsStateWithLifecycle()
    val dateMillis by viewModel.dateMillis.collectAsStateWithLifecycle()
    val ticketUri by viewModel.ticketUri.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val colors = FintrackTheme.colors

    val totals = calculatePurchaseTotals(products)

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

    Surface(modifier = modifier.fillMaxSize(), color = colors.celesteMist) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.adjust_ticket_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            item {
                Text(
                    text = stringResource(R.string.purchase_ticket_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.neutralWhite),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.celesteSoft, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = ticketUri ?: stringResource(R.string.purchase_ticket_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.celesteInk,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            item {
                PurchaseDataSection(
                    title = stringResource(R.string.purchase_data_title),
                    supermarket = supermarket,
                    onSupermarketChange = viewModel::setSupermarket,
                    dateText = formatDate(dateMillis),
                    timeText = formatTime(dateMillis),
                    onDateClick = openDatePicker,
                    onTimeClick = openTimePicker,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.records_products_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.celesteInk
                    )
                    Button(
                        onClick = { viewModel.addEmptyProduct() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.celesteBase,
                            contentColor = colors.neutralWhite
                        )
                    ) {
                        Text(text = stringResource(R.string.purchase_add_inline))
                    }
                }
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
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.celesteBase,
                        contentColor = colors.neutralWhite
                    )
                ) {
                    Text(text = stringResource(R.string.purchase_adjust_confirm))
                }
            }
        }
    }
}
