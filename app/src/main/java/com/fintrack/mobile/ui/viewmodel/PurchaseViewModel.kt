package com.fintrack.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.mobile.data.repository.NewProduct
import com.fintrack.mobile.data.repository.PurchaseRepository
import com.fintrack.mobile.ui.util.parseCents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class EditableProductDraft(
    val id: Long,
    val name: String,
    val code: String,
    val description: String,
    val quantity: String,
    val price: String,
    val discount: String,
)

class PurchaseViewModel(
    private val repository: PurchaseRepository,
) : ViewModel() {
    private val _supermarket = MutableStateFlow("")
    val supermarket: StateFlow<String> = _supermarket.asStateFlow()

    private val _dateMillis = MutableStateFlow(System.currentTimeMillis())
    val dateMillis: StateFlow<Long> = _dateMillis.asStateFlow()

    private val _ticketUri = MutableStateFlow<String?>(null)
    val ticketUri: StateFlow<String?> = _ticketUri.asStateFlow()

    private val _products = MutableStateFlow<List<EditableProductDraft>>(emptyList())
    val products: StateFlow<List<EditableProductDraft>> = _products.asStateFlow()

    private var nextProductId = 1L

    fun setSupermarket(value: String) {
        _supermarket.value = value
    }

    fun setDateMillis(value: Long) {
        _dateMillis.value = value
    }

    fun setTicketUri(value: String?) {
        _ticketUri.value = value
    }

    fun addEmptyProduct() {
        _products.value = _products.value + buildEmptyProduct()
    }

    fun addProduct(name: String, quantity: Int, priceCents: Long) {
        val product = EditableProductDraft(
            id = nextProductId++,
            name = name,
            code = "",
            description = "",
            quantity = quantity.toString(),
            price = formatCents(priceCents),
            discount = formatCents(0)
        )
        _products.value = _products.value + product
    }

    fun updateProduct(index: Int, updated: EditableProductDraft) {
        val current = _products.value
        if (index !in current.indices) return
        val mutable = current.toMutableList()
        mutable[index] = updated
        _products.value = mutable
    }

    fun removeProduct(index: Int) {
        val current = _products.value
        if (index !in current.indices) return
        val mutable = current.toMutableList()
        mutable.removeAt(index)
        _products.value = mutable
    }

    fun clearDraft() {
        _supermarket.value = ""
        _dateMillis.value = System.currentTimeMillis()
        _ticketUri.value = null
        _products.value = emptyList()
    }

    fun savePurchase(totalCents: Long) {
        val supermarketName = _supermarket.value.trim()
        val dateMillis = _dateMillis.value
        val products = _products.value.map { it.toNewProduct() }
        if (supermarketName.isBlank()) return
        viewModelScope.launch {
            repository.addPurchase(supermarketName, totalCents, dateMillis, products)
            clearDraft()
        }
    }

    private fun buildEmptyProduct(): EditableProductDraft {
        return EditableProductDraft(
            id = nextProductId++,
            name = "",
            code = "",
            description = "",
            quantity = "1",
            price = "0.00",
            discount = "0.00"
        )
    }

    private fun EditableProductDraft.toNewProduct(): NewProduct {
        val safeQuantity = quantity.toIntOrNull()?.coerceAtLeast(0) ?: 0
        return NewProduct(
            name = name,
            code = code,
            description = description,
            quantity = safeQuantity,
            priceCents = parseCents(price).coerceAtLeast(0),
            discountCents = parseCents(discount).coerceAtLeast(0)
        )
    }

    private fun formatCents(cents: Long): String {
        return String.format(Locale.US, "%.2f", cents / 100.0)
    }
}
