package com.undef.fintrackmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.components.FintrackSectionHeader
import com.undef.fintrackmobile.ui.components.home.*
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.util.formatCurrency
import com.undef.fintrackmobile.ui.viewmodel.HomeViewModel
import com.undef.fintrackmobile.ui.viewmodel.HomeUiState
import com.undef.fintrackmobile.ui.viewmodel.HomeUiData
import kotlinx.coroutines.delay

/**
 * HomeScreen: Dashboard principal de la aplicación.
 * Centraliza la visualización de gastos mensuales, métricas históricas, ofertas y compras recientes.
 * Refactorizado para usar componentes modulares y mantenibles.
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
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Error -> {
                val errorMsg = state.messageRes?.let { stringResource(it) } ?: state.message ?: stringResource(R.string.unknown_error)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.error_prefix, errorMsg), color = MaterialTheme.colorScheme.error)
                }
            }
            is HomeUiState.Success -> {
                HomeScreenContent(
                    data = state.data,
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
    val colors = FintrackTheme.colors

    // Configuración de ofertas estáticas
    val offers = remember(colors) { getStaticOffers(colors) }
    val offerListState = rememberLazyListState()
    var offerIndex by remember { mutableIntStateOf(0) }

    // Efecto de carrusel automático para ofertas.
    LaunchedEffect(offers.size) {
        while (true) {
            delay(3200)
            if (offers.isNotEmpty()) {
                offerIndex = (offerIndex + 1) % offers.size
                offerListState.animateScrollToItem(offerIndex)
            }
        }
    }

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

        // 5. Carrusel de ofertas
        item(key = "home_offers") {
            FintrackSectionHeader(
                title = R.string.home_offers_title, 
                subtitle = R.string.home_offers_subtitle
            )
            Spacer(modifier = Modifier.height(12.dp))
            HomeOfferCarousel(items = offers, state = offerListState)
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

        // 7. Desglose por categorías
        item(key = "home_categories") {
            FintrackSectionHeader(title = R.string.home_expenses_category)
            CategorySpendingSection()
        }

        // 8. Gastos por supermercado (Gráfico de barras)
        item(key = "home_supermarket_spending") {
            Spacer(modifier = Modifier.height(16.dp))
            FintrackSectionHeader(
                title = R.string.home_supermarket_spending_title, 
                subtitle = R.string.home_supermarket_spending_subtitle
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.celesteIce),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    SupermarketSpendingSection(
                        stats = data.supermarketMonthlyStats, 
                        currencyCode = currencyCode
                    )
                }
            }
        }
    }
}

private fun getStaticOffers(colors: com.undef.fintrackmobile.ui.theme.FintrackBrandColors) = listOf(
    OfferCardState(R.string.home_offer_1_title, R.string.home_offer_1_subtitle, R.string.home_offer_1_detail, colors.offerPeach),
    OfferCardState(R.string.home_offer_2_title, R.string.home_offer_2_subtitle, R.string.home_offer_2_detail, colors.offerLavender),
    OfferCardState(R.string.home_offer_3_title, R.string.home_offer_3_subtitle, R.string.home_offer_3_detail, colors.offerSand),
)
