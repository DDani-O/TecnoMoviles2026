package com.undef.fintrackmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.fintrackmobile.data.local.entity.PurchaseWithProducts
import com.undef.fintrackmobile.data.repository.PurchaseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext

/**
 * 🔟 SEALED CLASSES - UI State
 * Representan estados exhaustivos de la interfaz.
 * El compilador nos obliga a manejar todos los casos (Loading, Success, Error).
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: HomeUiData) : HomeUiState()
    data class Error(val message: String? = null, val messageRes: Int? = null) : HomeUiState()
}

data class HomeUiData(
    val monthlyTotalCents: Long,
    val monthAverageCents: Long,
    val weeklyStats: List<Long>,
    val supermarketMonthlyStats: List<Pair<String, Long>>,
    val recentPurchases: List<PurchaseWithProducts>,
    val insights: List<HomeInsight>,
    val historyStats: List<HistoryStat>
)

data class HomeInsight(
    val titleRes: Int,
    val subtitleRes: Int? = null,
    val subtitleArgs: List<Any> = emptyList(),
    val subtitle: String? = null,
    val iconType: InsightIcon,
    val category: InsightCategory
)

enum class InsightIcon { TRENDING_UP, RECEIPT, STORE, SHOPPING_CART }
enum class InsightCategory { SPENDING, HABIT, RECENT, LOYALTY }

data class HistoryStat(
    val titleRes: Int,
    val valueCents: Long? = null,
    val valueCount: Int? = null,
    val valueString: String? = null,
    val subtitleRes: Int? = null,
    val subtitle: String? = null,
)

/**
 * 9️⃣ VIEWMODEL Y STATEFLOW - Gestión de Estado
 * El ViewModel sobrevive a cambios de configuración (rotación).
 * Expone un StateFlow que es el 'Single Source of Truth' para la UI.
 */
class HomeViewModel(
    private val repository: PurchaseRepository,
) : ViewModel() {

    /**
     * combine() orquesta múltiples flujos de datos.
     * transformLatest() permite realizar procesamiento asincrónico (suspend) cada vez que los flujos emiten.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        repository.observePurchasesWithProducts(),
        repository.observePurchases()
    ) { purchasesWithProducts, allPurchases ->
        purchasesWithProducts to allPurchases
    }.transformLatest { (purchasesWithProducts, allPurchases) ->
        emit(HomeUiState.Loading)
        try {
            // Lógica de Negocio: Filtramos solo las compras del mes actual
            val calendar = Calendar.getInstance()
            val currentMonth = calendar[Calendar.MONTH]
            val currentYear = calendar[Calendar.YEAR]

            val monthPurchases = allPurchases.filter {
                val pCal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
                (pCal[Calendar.MONTH] == currentMonth && pCal[Calendar.YEAR] == currentYear)
            }

            val totalCents = monthPurchases.sumOf { it.totalCents }
            val avgCents = if (monthPurchases.isEmpty()) 0L else totalCents / monthPurchases.size
            
            // Procesamiento pesado del gráfico en un hilo de cómputo (Default)
            val weekly = groupPurchasesByFourWeeks(allPurchases.map { it.dateMillis to it.totalCents })
            
            val supermarketStats = calculateSupermarketStats(monthPurchases)
            val insights = generateInsights(allPurchases, purchasesWithProducts, totalCents)
            val history = generateHistoryStats(monthPurchases, totalCents, avgCents)

            // Retornamos el estado de Éxito con los datos procesados
            emit(
                HomeUiState.Success(
                    HomeUiData(
                        monthlyTotalCents = totalCents,
                        monthAverageCents = avgCents,
                        weeklyStats = weekly,
                        supermarketMonthlyStats = supermarketStats,
                        recentPurchases = purchasesWithProducts.take(4),
                        insights = insights,
                        historyStats = history
                    )
                )
            )
        } catch (e: Exception) {
            // Si algo falla en el cálculo, emitimos el estado de Error
            emit(HomeUiState.Error(
                message = e.message,
                messageRes = com.undef.fintrackmobile.R.string.error_home_loading
            ))
        }
    }.stateIn(
        scope = viewModelScope, 
        // WhileSubscribed(5000) optimiza recursos al pausar el flujo si la app va a background
        started = SharingStarted.WhileSubscribed(5_000), 
        initialValue = HomeUiState.Loading
    )

    /**
     * groupPurchasesByFourWeeks: Groups purchases into exactly 4 blocks based on the day of the month.
     * Week 1: 1-7, Week 2: 8-14, Week 3: 15-21, Week 4: 22-end.
     * Uses withContext(Dispatchers.Default) for asynchronous processing as requested.
     */
    private suspend fun groupPurchasesByFourWeeks(purchases: List<Pair<Long, Long>>): List<Long> = withContext(Dispatchers.Default) {
        // Inicializamos los 4 acumuladores para cada semana fija del mes
        var week1Amount = 0L // Días 1 al 7
        var week2Amount = 0L // Días 8 al 14
        var week3Amount = 0L // Días 15 al 21
        var week4Amount = 0L // Días 22 hasta fin de mes

        val calendar = Calendar.getInstance()
        val currentMonth = calendar[Calendar.MONTH]
        val currentYear = calendar[Calendar.YEAR]

        purchases.forEach { (dateMillis, amount) ->
            calendar.timeInMillis = dateMillis
            
            // Validamos que la compra pertenezca estrictamente al mes y año actual
            if (calendar[Calendar.MONTH] == currentMonth && calendar[Calendar.YEAR] == currentYear) {
                // Clasificación por rangos de días fijos según requerimiento técnico
                when (calendar[Calendar.DAY_OF_MONTH]) {
                    in 1..7 -> week1Amount += amount
                    in 8..14 -> week2Amount += amount
                    in 15..21 -> week3Amount += amount
                    else -> week4Amount += amount // Día 22 en adelante hasta el final del mes
                }
            }
        }

        // Retornamos la lista con exactamente los 4 valores requeridos para el gráfico
        listOf(week1Amount, week2Amount, week3Amount, week4Amount)
    }

    private fun calculateSupermarketStats(purchases: List<com.undef.fintrackmobile.data.local.entity.PurchaseEntity>): List<Pair<String, Long>> {
        return purchases.groupBy { it.supermarketName }
            .map { entry -> entry.key to entry.value.sumOf { it.totalCents } }
            .sortedByDescending { it.second }
            .take(3)
    }

    private fun generateInsights(
        purchases: List<com.undef.fintrackmobile.data.local.entity.PurchaseEntity>,
        purchasesWithProducts: List<PurchaseWithProducts>,
        totalCents: Long
    ): List<HomeInsight> {
        val list = mutableListOf<HomeInsight>()

        // Insight de hábitos
        if (purchasesWithProducts.size >= 2) {
            val history = purchasesWithProducts.drop(1)
            val avgItems = history.sumOf { it.products.size }.toFloat() / history.size
            val currentItems = purchasesWithProducts.first().products.size
            if (currentItems > (avgItems * 1.2f) && currentItems > 2) {
                val percent = (((currentItems - avgItems) / avgItems) * 100).toInt()
                list.add(HomeInsight(
                    titleRes = com.undef.fintrackmobile.R.string.home_news_habit_change_title,
                    subtitleRes = com.undef.fintrackmobile.R.string.home_news_habit_change_subtitle,
                    subtitleArgs = listOf(currentItems, percent),
                    iconType = InsightIcon.SHOPPING_CART,
                    category = InsightCategory.HABIT
                ))
            }
        }

        // Insight de gasto total
        if (totalCents > 0L) {
            list.add(HomeInsight(
                titleRes = com.undef.fintrackmobile.R.string.home_news_spending_title,
                subtitleRes = com.undef.fintrackmobile.R.string.home_news_spending_subtitle,
                iconType = InsightIcon.TRENDING_UP,
                category = InsightCategory.SPENDING
            ))
        }

        // Insight de última tienda
        if (purchases.isNotEmpty()) {
            list.add(HomeInsight(
                titleRes = com.undef.fintrackmobile.R.string.home_news_recent_title,
                subtitleRes = com.undef.fintrackmobile.R.string.home_news_recent_subtitle,
                subtitleArgs = listOf(purchases.first().supermarketName),
                iconType = InsightIcon.STORE,
                category = InsightCategory.RECENT
            ))
        }

        // Insight de lealtad
        if (purchases.size >= 2) {
            val repeatedStore = purchases.groupingBy { it.supermarketName }.eachCount().maxByOrNull { it.value }
            if ((repeatedStore?.value ?: 0) >= 2) {
                list.add(HomeInsight(
                    titleRes = com.undef.fintrackmobile.R.string.home_news_loyalty_title,
                    subtitleRes = com.undef.fintrackmobile.R.string.home_news_loyalty_subtitle,
                    subtitleArgs = listOf(repeatedStore?.value ?: 0, repeatedStore?.key ?: ""),
                    iconType = InsightIcon.RECEIPT,
                    category = InsightCategory.LOYALTY
                ))
            }
        }

        return list
    }

    private fun generateHistoryStats(
        purchases: List<com.undef.fintrackmobile.data.local.entity.PurchaseEntity>,
        totalCents: Long,
        avgCents: Long
    ): List<HistoryStat> {
        val biggestPurchase = purchases.maxByOrNull { it.totalCents }
        return listOf(
            HistoryStat(
                titleRes = com.undef.fintrackmobile.R.string.home_stats_month_total,
                valueCents = totalCents,
                subtitleRes = com.undef.fintrackmobile.R.string.home_stats_month_total_subtitle
            ),
            HistoryStat(
                titleRes = com.undef.fintrackmobile.R.string.home_stats_avg_ticket,
                valueCents = avgCents,
                subtitleRes = com.undef.fintrackmobile.R.string.home_stats_avg_ticket_subtitle
            ),
            HistoryStat(
                titleRes = com.undef.fintrackmobile.R.string.home_stats_biggest_ticket,
                valueCents = biggestPurchase?.totalCents,
                subtitle = biggestPurchase?.supermarketName,
                subtitleRes = if (biggestPurchase == null) com.undef.fintrackmobile.R.string.home_stats_no_data else null
            ),
            HistoryStat(
                titleRes = com.undef.fintrackmobile.R.string.home_stats_total_purchases,
                valueCount = purchases.size,
                subtitleRes = com.undef.fintrackmobile.R.string.home_stats_total_purchases_subtitle
            )
        )
    }

    init {
        // 8️⃣ CORRUTINAS - viewModelScope
        // Lanzamos una corrutina bound al ciclo de vida del ViewModel.
        // Se cancelará automáticamente si el usuario sale de esta pantalla.
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
    }
}
