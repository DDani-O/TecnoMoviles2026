package com.fintrack.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.data.repository.NewProduct
import com.fintrack.mobile.ui.components.LabeledTextField
import com.fintrack.mobile.ui.components.ProductLineItemCard
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.util.parseCents
import com.fintrack.mobile.ui.viewmodel.PurchaseViewModel

@Composable
fun AddProductsScreen(
    viewModel: PurchaseViewModel,
    currencyCode: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var quantityInput by rememberSaveable { mutableStateOf("") }
    var priceInput by rememberSaveable { mutableStateOf("") }
    val products by viewModel.products.collectAsStateWithLifecycle()

    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.add_products_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            item {
                LabeledTextField(
                    value = name,
                    onValueChange = { name = it },
                    labelRes = R.string.label_product_name,
                    placeholderRes = R.string.placeholder_product_name,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                LabeledTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    labelRes = R.string.label_product_quantity,
                    placeholderRes = R.string.placeholder_product_quantity,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                LabeledTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    labelRes = R.string.label_product_price,
                    placeholderRes = R.string.placeholder_product_price,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Button(
                    onClick = {
                        val qty = quantityInput.toIntOrNull() ?: 0
                        val cents = parseCents(priceInput)
                        if (name.isNotBlank() && (qty > 0) && (cents > 0L)) {
                            viewModel.addProduct(
                                NewProduct(
                                    name = name,
                                    quantity = qty,
                                    priceCents = cents,
                                ),
                            )
                            name = ""
                            quantityInput = ""
                            priceInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.add_products_button))
                }
            }
            if (products.isNotEmpty()) {
                items(products, key = { it.name + it.priceCents }) { product ->
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
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.action_done))
                }
            }
        }
    }
}
