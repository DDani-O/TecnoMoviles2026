package com.fintrack.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.mobile.data.local.entity.PurchaseEntity
import com.fintrack.mobile.data.repository.PurchaseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordsViewModel(
    private val repository: PurchaseRepository
) : ViewModel() {
    val purchases: StateFlow<List<PurchaseEntity>> = repository.observePurchases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
    }
}
