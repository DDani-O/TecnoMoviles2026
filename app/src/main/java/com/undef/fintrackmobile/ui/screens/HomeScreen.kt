package com.undef.fintrackmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.components.FintrackScreenState
import com.undef.fintrackmobile.ui.components.FintrackSectionHeader
import com.undef.fintrackmobile.ui.components.home.*
import com.undef.fintrackmobile.ui.util.formatCurrency
import com.undef.fintrackmobile.ui.viewmodel.HomeViewModel
import com.undef.fintrackmobile.ui.viewmodel.HomeUiState
import com.undef.fintrackmobile.ui.viewmodel.HomeUiData

/**
 * HomeScreen: Dashboard principal de la aplicación.
 * Refactorizada para usar FintrackScreenState.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    displayName: String,
    currencyCode: String,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    profileImageUri: String? = null,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Estado para controlar la visibilidad del BottomSheet de notificaciones (State Hoisting).
    val showNotificationsSheet = rememberSaveable { mutableStateOf(value = false) }
    val sheetState = rememberModalBottomSheetState()

    if (showNotificationsSheet.value) {
        NotificationsBottomSheet(sheetState = sheetState) {
            showNotificationsSheet.value = false
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        FintrackScreenState(
            isLoading = uiState is HomeUiState.Loading,
            errorMessage = if (uiState is HomeUiState.Error) {
                val state = uiState as HomeUiState.Error
                state.messageRes?.let { stringResource(it) } ?: state.message
            } else {
                null
            },
        ) {
            if (uiState is HomeUiState.Success) {
                HomeScreenContent(
                    data = (uiState as HomeUiState.Success).data,
                    displayName = displayName,
                    currencyCode = currencyCode,
                    profileImageUri = profileImageUri,
                    onNotificationsClick = {
                        onNotificationsClick()
                        showNotificationsSheet.value = true
                    },
                    onProfileClick = onProfileClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    data: HomeUiData,
    displayName: String,
    currencyCode: String,
    profileImageUri: String?,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val greetingName = displayName.ifBlank { stringResource(R.string.default_user_name) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Cabecera
        item(key = "home_header") {
            HomeHeader(
                displayName = greetingName,
                profileImageUri = profileImageUri,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick,
            )
        }

        // 2. Noticias / Insights
        item(key = "home_news") {
            HomeNewsSection(
                insights = data.insights, 
                currencyCode = currencyCode, 
                totalCents = data.monthlyTotalCents
            )
        }

        // 3. Tarjeta de resumen mensual
        item(key = "home_summary") {
            HomeSummaryCard(
                totalFormatted = formatCurrency(data.monthlyTotalCents, currencyCode),
                averageFormatted = formatCurrency(data.monthAverageCents, currencyCode),
                weeklyStats = data.weeklyStats,
                currencyCode = currencyCode,
            )
        }

        // 4. Historial de métricas
        item(key = "home_stats_history") {
            FintrackSectionHeader(
                title = R.string.home_stats_history_title, 
                subtitle = R.string.home_stats_history_subtitle
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(data.historyStats, key = { it.titleRes }) { stat ->
                    HistoryStatCard(stat = stat, currencyCode = currencyCode)
                }
            }
        }

        // 6. Compras recientes (Tickets)
        item(key = "home_recent_history") {
            FintrackSectionHeader(
                title = R.string.home_recent_history, 
                subtitle = R.string.home_recent_history_subtitle
            )
            Spacer(modifier = Modifier.height(12.dp))
            HomeTicketCarousel(purchases = data.recentPurchases, currencyCode = currencyCode)
        }
    }
}
