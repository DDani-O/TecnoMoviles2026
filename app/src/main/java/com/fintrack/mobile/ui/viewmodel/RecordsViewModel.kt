package com.fintrack.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.mobile.data.local.entity.PurchaseEntity
import com.fintrack.mobile.data.local.entity.PurchaseWithProducts
import com.fintrack.mobile.data.local.entity.ProductEntity
import com.fintrack.mobile.data.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class RecordsUiState {
    object Loading : RecordsUiState()
    data class Success(
        val purchases: List<PurchaseWithProducts>,
        val filteredStats: StatsData
    ) : RecordsUiState()
    data class Error(val message: String) : RecordsUiState()
}

data class StatsData(
    val totalSpentCents: Long,
    val averageSpentCents: Long,
    val ticketCount: Int,
    val supermarketDistribution: List<Pair<String, Long>>,
    val productRanking: List<ProductRank>
)

data class ProductRank(
    val name: String,
    val quantity: Int,
    val totalCents: Long
)

enum class PeriodFilter { WEEK, MONTH, YEAR }

class RecordsViewModel(
    private val repository: PurchaseRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(PeriodFilter.MONTH)
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val uiState: StateFlow<RecordsUiState> = combine(
        repository.observePurchasesWithProducts(),
        _selectedPeriod,
        _selectedMonth,
        _selectedYear
    ) { purchases, period, month, year ->
        try {
            val filtered = filterPurchases(purchases, period, month, year)
            val stats = calculateStats(filtered)
            RecordsUiState.Success(purchases, stats)
        } catch (e: Exception) {
            RecordsUiState.Error(e.message ?: "Error desconocido")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordsUiState.Loading)

    init {
        viewModelScope.launch {
            repository.seedIfEmpty(force = true)
        }
    }

    fun setPeriod(period: PeriodFilter) { _selectedPeriod.value = period }
    fun setMonthYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    private fun filterPurchases(
        all: List<PurchaseWithProducts>,
        period: PeriodFilter,
        month: Int,
        year: Int
    ): List<PurchaseWithProducts> {
        val now = System.currentTimeMillis()
        return all.filter { item ->
            val cal = Calendar.getInstance().apply { timeInMillis = item.purchase.dateMillis }
            when (period) {
                PeriodFilter.WEEK -> item.purchase.dateMillis >= now - (7L * 24 * 60 * 60 * 1000)
                PeriodFilter.MONTH -> cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
                PeriodFilter.YEAR -> cal.get(Calendar.YEAR) == year
            }
        }
    }

    private fun calculateStats(filtered: List<PurchaseWithProducts>): StatsData {
        val totalSpent = filtered.sumOf { it.purchase.totalCents }
        val avg = if (filtered.isNotEmpty()) totalSpent / filtered.size else 0L
        
        val distribution = filtered.groupBy { it.purchase.supermarketName }
            .mapValues { it.value.sumOf { p -> p.purchase.totalCents } }
            .toList()
            .sortedByDescending { it.second }

        val ranking = filtered.asSequence()
            .flatMap { it.products }
            .groupBy { it.name }
            .map { (name, items) ->
                ProductRank(name, items.sumOf { it.quantity }, items.sumOf { it.priceCents * it.quantity.toLong() })
            }
            .sortedByDescending { it.quantity }
            .take(5)
            .toList()

        return StatsData(totalSpent, avg, filtered.size, distribution, ranking)
    }

    fun updatePurchase(purchase: PurchaseEntity, products: List<ProductEntity>) {
        viewModelScope.launch {
            repository.updatePurchase(purchase, products)
        }
    }

    fun deletePurchase(purchase: PurchaseEntity) {
        viewModelScope.launch {
            repository.deletePurchase(purchase)
        }
    }
}
