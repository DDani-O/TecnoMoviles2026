package com.fintrack.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.mobile.data.local.entity.PurchaseEntity
import com.fintrack.mobile.data.repository.PurchaseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PurchaseRepository
) : ViewModel() {
    val recentPurchases: StateFlow<List<PurchaseEntity>> = repository.observePurchases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthlyTotalCents: StateFlow<Long> = repository.observePurchases()
        .map { purchases -> purchases.sumOf { it.totalCents } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
    }
}
