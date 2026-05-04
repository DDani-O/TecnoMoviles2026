package com.fintrack.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.mobile.data.repository.NewProduct
import com.fintrack.mobile.data.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val repository: PurchaseRepository,
) : ViewModel() {
    private val _products = MutableStateFlow<List<NewProduct>>(emptyList())
    val products: StateFlow<List<NewProduct>> = _products.asStateFlow()

    fun addProduct(product: NewProduct) {
        _products.value += product
    }

    fun clearProducts() {
        _products.value = emptyList()
    }

    fun savePurchase(supermarketName: String, totalCents: Long) {
        viewModelScope.launch {
            repository.addPurchase(supermarketName, totalCents, _products.value)
            clearProducts()
        }
    }
}
