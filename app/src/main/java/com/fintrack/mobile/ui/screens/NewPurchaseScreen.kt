package com.fintrack.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.components.LabeledTextField
import com.fintrack.mobile.ui.components.ProductLineItemCard
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.util.parseCents
import com.fintrack.mobile.ui.viewmodel.PurchaseViewModel

@Composable
fun NewPurchaseScreen(
    currencyCode: String,
    viewModel: PurchaseViewModel,
    onAddProducts: () -> Unit,
    onAdjustTicket: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var supermarket by rememberSaveable { mutableStateOf("") }
    var totalInput by rememberSaveable { mutableStateOf("") }
    val products by viewModel.products.collectAsStateWithLifecycle()

    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.purchase_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            item {
                LabeledTextField(
                    value = supermarket,
                    onValueChange = { supermarket = it },
                    labelRes = R.string.label_supermarket,
                    placeholderRes = R.string.placeholder_supermarket,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                LabeledTextField(
                    value = totalInput,
                    onValueChange = { totalInput = it },
                    labelRes = R.string.label_total,
                    placeholderRes = R.string.placeholder_total,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onAddProducts) {
                        Text(text = stringResource(R.string.purchase_add_products))
                    }
                    OutlinedButton(onClick = onAdjustTicket) {
                        Text(text = stringResource(R.string.purchase_adjust_ticket))
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.purchase_products_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (products.isEmpty()) {
                item {
                    Text(text = stringResource(R.string.add_products_empty))
                }
            } else {
                items(products) { product ->
                    ProductLineItemCard(
                        name = product.name,
                        lineItemText = stringResource(
                            R.string.product_line_item,
                            product.quantity,
                            formatCurrency(product.priceCents, currencyCode)
                        )
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        val totalCents = parseCents(totalInput)
                        if ((totalCents > 0L) && supermarket.isNotBlank()) {
                            viewModel.savePurchase(supermarket, totalCents)
                            totalInput = ""
                            supermarket = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.purchase_save))
                }
            }
        }
    }
}
