package com.undef.fintrackmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.data.local.entity.PurchaseEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseWithProducts
import com.undef.fintrackmobile.ui.components.FintrackScreenState
import com.undef.fintrackmobile.ui.components.FintrackSectionHeader
import com.undef.fintrackmobile.ui.components.records.*
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.viewmodel.*
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * RecordsScreen: Pantalla que gestiona el historial de compras y estadísticas detalladas.
 * Refactorizada para usar FintrackScreenState y componentes modulares.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    currencyCode: String,
    viewModel: RecordsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = FintrackTheme.colors

    // Estado local para navegación y diálogos
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val editingPurchaseState = remember { mutableStateOf<PurchaseWithProducts?>(null) }
    val deleteTargetState = remember { mutableStateOf<PurchaseEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.celesteMist),
    ) {
        FintrackScreenState(
            isLoading = uiState is RecordsUiState.Loading,
            errorMessage = if (uiState is RecordsUiState.Error) {
                val state = uiState as RecordsUiState.Error
                state.messageRes?.let { stringResource(it) } ?: state.message
            } else null
        ) {
            if (uiState is RecordsUiState.Success) {
                val state = uiState as RecordsUiState.Success
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.records_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.celesteDeep
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = colors.celesteMist,
                        contentColor = colors.celesteDeep,
                        indicator = {
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(selectedTab),
                                color = colors.celesteDeep
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(stringResource(R.string.records_tab_history)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(stringResource(R.string.records_tab_stats)) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedTab == 0) {
                        HistoryTab(
                            purchases = state.purchases,
                            currencyCode = currencyCode,
                            onEdit = { editingPurchaseState.value = it }
                        ) { deleteTargetState.value = it.purchase }
                    } else {
                        StatsTab(
                            statsData = state.filteredStats,
                            currencyCode = currencyCode,
                            viewModel = viewModel,
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }

    val saveSuccessMsg = stringResource(R.string.records_save_success)
    val deleteSuccessMsg = stringResource(R.string.records_delete_success)

    // Hoja modal para edición de compras
    editingPurchaseState.value?.let { purchase ->
        EditPurchaseSheet(
            purchase = purchase,
            currencyCode = currencyCode,
            onDismiss = { editingPurchaseState.value = null },
            onSave = { updatedPurchase, updatedProducts ->
                viewModel.updatePurchase(updatedPurchase, updatedProducts)
                editingPurchaseState.value = null
                scope.launch {
                    snackbarHostState.showSnackbar(saveSuccessMsg)
                }
            }
        )
    }

    // Diálogo de confirmación para eliminación
    deleteTargetState.value?.let { target ->
        DeleteConfirmationDialog(
            onDismiss = { deleteTargetState.value = null },
            onConfirm = {
                viewModel.deletePurchase(target)
                deleteTargetState.value = null
                scope.launch {
                    snackbarHostState.showSnackbar(deleteSuccessMsg)
                }
            }
        )
    }
}

/**
 * HistoryTab: Muestra una lista de registros de compra.
 */
@Composable
private fun HistoryTab(
    purchases: List<PurchaseWithProducts>,
    currencyCode: String,
    onEdit: (PurchaseWithProducts) -> Unit,
    onDelete: (PurchaseWithProducts) -> Unit,
) {
    if (purchases.isEmpty()) {
        EmptyHistoryCard()
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        items(purchases, key = { it.purchase.id }) { purchase ->
            HistoryRecordCard(
                purchase = purchase,
                currencyCode = currencyCode,
                onEdit = { onEdit(purchase) },
                onDelete = { onDelete(purchase) }
            )
        }
    }
}

/**
 * StatsTab: Muestra estadísticas avanzadas y filtros temporales.
 */
@Composable
private fun StatsTab(
    statsData: StatsData,
    currencyCode: String,
    viewModel: RecordsViewModel,
) {
    var selectedPeriod by rememberSaveable { mutableStateOf(PeriodFilter.MONTH) }
    
    val calendar = Calendar.getInstance()
    var selectedMonthIndex by rememberSaveable { mutableIntStateOf(calendar[Calendar.MONTH]) }
    var selectedYear by rememberSaveable { mutableIntStateOf(calendar[Calendar.YEAR]) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FintrackSectionHeader(
                title = R.string.records_stats_period_title,
                subtitle = stringResource(R.string.records_stats_period_subtitle)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PeriodFilterRow(selectedPeriod = selectedPeriod) {
                    selectedPeriod = it
                    viewModel.setPeriod(it)
                }

                if (selectedPeriod == PeriodFilter.MONTH) {
                    SpecificMonthSelector(
                        selectedMonth = selectedMonthIndex,
                        selectedYear = selectedYear,
                        onMonthYearSelected = { month, year ->
                            selectedMonthIndex = month
                            selectedYear = year
                            viewModel.setMonthYear(month, year)
                        }
                    )
                } else if (selectedPeriod == PeriodFilter.YEAR) {
                    SpecificYearSelector(
                        selectedYear = selectedYear,
                        onYearSelected = {
                            selectedYear = it
                            viewModel.setMonthYear(selectedMonthIndex, it)
                        }
                    )
                }
            }
        }

        StatsSummaryCard(
            selectedPeriod = selectedPeriod,
            selectedMonthIndex = selectedMonthIndex,
            selectedYear = selectedYear,
            totalSpent = statsData.totalSpentCents,
            average = statsData.averageSpentCents,
            ticketCount = statsData.ticketCount,
            currencyCode = currencyCode
        )

        FintrackSectionHeader(
            title = R.string.records_stats_distribution_title,
            subtitle = stringResource(R.string.records_stats_distribution_subtitle)
        )
        SupermarketPieChartCard(
            distribution = statsData.supermarketDistribution,
            currencyCode = currencyCode
        )

        FintrackSectionHeader(
            title = R.string.records_stats_ranking_title,
            subtitle = stringResource(R.string.records_stats_ranking_subtitle)
        )
        ProductPodiumCard(ranking = statsData.productRanking)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun EmptyHistoryCard() {
    val colors = FintrackTheme.colors
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.celesteMist),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.records_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.celesteInk.copy(alpha = 0.6f)
            )
        }
    }
}
