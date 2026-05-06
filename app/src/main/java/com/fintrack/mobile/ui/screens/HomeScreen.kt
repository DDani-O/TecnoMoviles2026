package com.fintrack.mobile.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.data.local.entity.PurchaseWithProducts
import com.fintrack.mobile.ui.components.TitleSubtitleCard
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.util.formatDate
import com.fintrack.mobile.ui.util.getSupermarketColors
import com.fintrack.mobile.ui.viewmodel.HomeViewModel
import com.fintrack.mobile.ui.viewmodel.HomeUiState
import com.fintrack.mobile.ui.viewmodel.HomeUiData
import com.fintrack.mobile.ui.viewmodel.HomeInsight
import com.fintrack.mobile.ui.viewmodel.HistoryStat
import com.fintrack.mobile.ui.viewmodel.InsightIcon
import com.fintrack.mobile.ui.viewmodel.InsightCategory
import kotlinx.coroutines.delay

/**
 * HomeScreen: Dashboard principal de la aplicación.
 * Centraliza la visualización de gastos mensuales, métricas históricas, ofertas y compras recientes.
 * Utiliza un sistema de UiState para manejar estados de carga, éxito y error.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    displayName: String,
    currencyCode: String,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    profileImagePainter: Painter? = null,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Estado para controlar la visibilidad del BottomSheet de notificaciones (State Hoisting).
    // Usamos rememberSaveable para que el estado persista ante cambios de configuración.
    // Usamos el objeto MutableState directamente para evitar la advertencia "Assigned value is never read" del IDE.
    val showNotificationsSheet = rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showNotificationsSheet.value) {
        NotificationsBottomSheet(
            sheetState = sheetState,
            onDismiss = { showNotificationsSheet.value = false }
        )
    }

    Surface(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Cargando dashboard...", style = MaterialTheme.typography.bodyLarge)
                }
            }
            is HomeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${state.message}", color = Color.Red)
                }
            }
            is HomeUiState.Success -> {
                HomeScreenContent(
                    data = state.data,
                    displayName = displayName,
                    currencyCode = currencyCode,
                    profileImagePainter = profileImagePainter,
                    onNotificationsClick = {
                        // Al hacer clic en notificaciones, ejecutamos la acción externa y mostramos el sheet local.
                        onNotificationsClick()
                        showNotificationsSheet.value = true
                    },
                    onProfileClick = onProfileClick
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
    profileImagePainter: Painter?,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val greetingName = displayName.ifBlank { stringResource(R.string.default_user_name) }

    // Configuración de ofertas.
    val offers = remember { getStaticOffers() }
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
        // Cabecera
        item {
            HeaderRow(
                displayName = greetingName,
                profileImagePainter = profileImagePainter,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick,
            )
        }

        // Noticias / Insights
        item {
            NewsFeedSection(insights = data.insights, currencyCode = currencyCode, totalCents = data.monthlyTotalCents)
        }

        // Tarjeta de resumen mensual
        item {
            SummaryCard(
                totalFormatted = formatCurrency(data.monthlyTotalCents, currencyCode),
                averageFormatted = formatCurrency(data.monthAverageCents, currencyCode),
                weeklyStats = data.weeklyStats,
                currencyCode = currencyCode,
            )
        }

        // Historial de métricas
        item {
            SectionHeader(title = R.string.home_stats_history_title, subtitle = R.string.home_stats_history_subtitle)
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

        // Carrusel de ofertas
        item {
            SectionHeader(title = R.string.home_offers_title, subtitle = R.string.home_offers_subtitle)
            Spacer(modifier = Modifier.height(12.dp))
            OfferCarousel(items = offers, state = offerListState)
        }

        // Compras recientes
        item {
            SectionHeader(title = R.string.home_recent_history, subtitle = R.string.home_recent_history_subtitle)
            Spacer(modifier = Modifier.height(12.dp))
            TicketCarousel(purchases = data.recentPurchases, currencyCode = currencyCode)
        }

        // Desglose por categorías
        item {
            SectionHeader(title = R.string.home_expenses_category, subtitle = null)
            CategorySection()
        }

        // Gastos por supermercado
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = R.string.home_supermarket_spending_title, subtitle = R.string.home_supermarket_spending_subtitle)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CelesteIce),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    SupermarketMonthlySpendingSection(stats = data.supermarketMonthlyStats, currencyCode = currencyCode)
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(
    displayName: String,
    profileImagePainter: Painter?,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val notificationCount = 1
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onProfileClick() }
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(displayName = displayName, profileImagePainter = profileImagePainter)
            Spacer(modifier = Modifier.size(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.home_greeting, displayName),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(R.string.home_welcome),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        BadgedBox(
            badge = {
                Badge(
                    modifier = Modifier.padding(4.dp),
                    containerColor = CelesteDeep
                ) {
                    Text(text = notificationCount.toString(), color = Color.White)
                }
            }
        ) {
            Surface(
                shape = CircleShape,
                color = CelestePale,
                modifier = Modifier.size(48.dp)
            ) {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = stringResource(R.string.home_notifications),
                        tint = CelesteDeep
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    displayName: String,
    profileImagePainter: Painter?,
) {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        color = CelestePale,
    ) {
        if (profileImagePainter != null) {
            Image(
                painter = profileImagePainter,
                contentDescription = stringResource(R.string.home_profile_image),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initialsFor(displayName),
                    style = MaterialTheme.typography.titleMedium,
                    color = CelesteDeep,
                )
            }
        }
    }
}

@Composable
private fun NewsFeedSection(
    insights: List<HomeInsight>,
    currencyCode: String,
    totalCents: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = R.string.home_news_title, subtitle = R.string.home_news_subtitle)
        if (insights.isEmpty()) {
            TitleSubtitleCard(
                title = stringResource(R.string.home_news_empty_title),
                subtitle = stringResource(R.string.home_news_empty_subtitle),
                containerColor = PastelGreenPale,
                contentPadding = 16.dp,
                titleStyle = MaterialTheme.typography.titleMedium,
                subtitleStyle = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                insights.forEach { insight ->
                    InsightCard(insight = insight, currencyCode = currencyCode, totalCents = totalCents)
                }
            }
        }
    }
}

@Composable
private fun InsightCard(insight: HomeInsight, currencyCode: String, totalCents: Long) {
    // Unificamos el color a un verde pastel para todas las noticias.
    val accent = PastelGreenDeep
    
    val icon = when (insight.iconType) {
        InsightIcon.TRENDING_UP -> Icons.AutoMirrored.Filled.TrendingUp
        InsightIcon.RECEIPT -> Icons.AutoMirrored.Filled.ReceiptLong
        InsightIcon.STORE -> Icons.Filled.Storefront
        InsightIcon.SHOPPING_CART -> Icons.Filled.ShoppingCart
    }

    val subtitle = if (insight.category == InsightCategory.SPENDING) {
        stringResource(R.string.home_news_spending_value, formatCurrency(totalCents, currencyCode))
    } else {
        insight.subtitle
    }

    BorderedCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accent,
        cornerRadius = 16f,
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = accent,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(insight.titleRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    )
}

@Composable
private fun HistoryStatCard(stat: HistoryStat, currencyCode: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .border(
                width = 1.dp,
                color = IndicatorBorder,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = IndicatorBg),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(stat.titleRes), style = MaterialTheme.typography.labelMedium)
            
            val valueText = when {
                stat.valueCents != null -> formatCurrency(stat.valueCents, currencyCode)
                stat.valueCount != null -> stat.valueCount.toString()
                else -> stat.valueString ?: stringResource(R.string.home_stats_no_data)
            }
            
            Text(text = valueText, style = MaterialTheme.typography.titleLarge)
            val subtitle = stat.subtitleRes?.let { stringResource(it) } ?: stat.subtitle ?: ""
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OfferCarousel(
    items: List<OfferCardState>,
    state: androidx.compose.foundation.lazy.LazyListState,
) {
    LazyRow(
        state = state,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(end = 16.dp),
    ) {
        items(items, key = { it.titleRes }) { offer ->
            OfferCard(offer = offer)
        }
    }
}

@Composable
private fun OfferCard(offer: OfferCardState) {
    BorderedCard(
        modifier = Modifier.fillMaxWidth(0.84f),
        accentColor = offer.accent,
        cornerRadius = 22f,
        containerAlpha = 0.16f,
        content = {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = stringResource(offer.titleRes), style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(offer.subtitleRes), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stringResource(offer.detailRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    )
}

@Composable
private fun TicketCarousel(
    purchases: List<PurchaseWithProducts>,
    currencyCode: String,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        items(purchases, key = { it.purchase.id }) { purchase ->
            TicketCard(purchase = purchase, currencyCode = currencyCode)
        }
    }
}

@Composable
private fun TicketCard(
    purchase: PurchaseWithProducts,
    currencyCode: String,
) {
    val colors = getSupermarketColors(purchase.purchase.supermarketName)
    val (bgColor, accentColor, logoRes) = colors

    Card(
        modifier = Modifier
            .width(320.dp)
            .height(450.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    logoRes?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = purchase.purchase.supermarketName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }
                
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = formatDate(purchase.purchase.dateMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = accentColor.copy(alpha = 0.1f)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                purchase.products.forEach { product ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = product.name.uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "PRECIO UNITARIO: ${formatCurrency(product.priceCents, currencyCode)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "CANTIDAD: ${product.quantity}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(0.3f),
                            thickness = 0.5.dp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = accentColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.home_ticket_total), color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatCurrency(purchase.purchase.totalCents, currencyCode),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun BorderedCard(
    modifier: Modifier = Modifier,
    accentColor: Color,
    cornerRadius: Float,
    containerAlpha: Float = 0.12f,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(cornerRadius.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = containerAlpha)),
        shape = RoundedCornerShape(cornerRadius.dp),
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CelesteMist,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.home_notifications),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CelesteDeep
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CelesteSoft, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CelestePastel),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(shape = CircleShape, color = CelestePale, modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Filled.CardGiftcard, contentDescription = null, tint = CelesteDeep)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "¡Regalo de bienvenida!", fontWeight = FontWeight.Bold, color = CelesteDeep)
                        Text(text = "Tienes un 10% de descuento en tu próxima compra.", color = CelesteInk)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalFormatted: String,
    averageFormatted: String,
    weeklyStats: List<Long>,
    currencyCode: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, CelesteDeep.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.home_month_summary),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = CelesteDeep
                )
                Text(
                    text = stringResource(R.string.home_month_summary_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(0.5f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryMetric(label = stringResource(R.string.home_month_total_label), value = totalFormatted, emphasized = true)
                    SummaryMetric(label = stringResource(R.string.home_month_average_label), value = averageFormatted)
                }
                Box(modifier = Modifier.weight(0.5f)) {
                    BarChart(
                        values = weeklyStats.map { it.toFloat() },
                        labels = (1..weeklyStats.size).map { "S$it" },
                        amounts = weeklyStats.map { formatCurrency(it, currencyCode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, emphasized: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = CelesteDeep.copy(alpha = 0.75f))
        Text(
            text = value,
            style = if (emphasized) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (emphasized) CelesteDeep else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BarChart(values: List<Float>, labels: List<String>, amounts: List<String>) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            values.forEachIndexed { index, value ->
                val ratio = value / maxValue
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = amounts[index],
                        style = MaterialTheme.typography.labelSmall,
                        color = CelesteBase,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((ratio * 90).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(CelesteBase),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = labels[index], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SupermarketMonthlySpendingSection(stats: List<Pair<String, Long>>, currencyCode: String) {
    val maxSpent = stats.maxOfOrNull { it.second }?.toFloat()?.coerceAtLeast(1f) ?: 1f
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        stats.forEach { (name, total) ->
            val ratio = total.toFloat() / maxSpent
            val barColor = getSupermarketColors(name).accentColor
            
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold)
                    Text(text = formatCurrency(total, currencyCode), style = MaterialTheme.typography.titleMedium, color = barColor, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(CircleShape).background(barColor.copy(alpha = 0.18f))) {
                    Box(modifier = Modifier.fillMaxWidth(ratio).fillMaxSize().clip(CircleShape).background(barColor))
                }
            }
        }
    }
}

@Composable
private fun CategorySection() {
    val categories = remember {
        listOf(
            CategoryStat(R.string.category_food, 0.45f, Color(0xFF7BB9F1)),
            CategoryStat(R.string.category_cleaning, 0.2f, Color(0xFFFFD48A)),
            CategoryStat(R.string.category_home, 0.15f, Color(0xFFF3A1A1)),
            CategoryStat(R.string.category_other, 0.2f, Color(0xFFD1A6E8)),
        )
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(0.55f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { stat ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(modifier = Modifier.size(16.dp), shape = CircleShape, color = stat.color) {}
                    Text(text = stringResource(stat.labelRes), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(text = "${(stat.percent * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            }
        }
        Box(modifier = Modifier.weight(0.45f).height(180.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2.4f
                val centerX = size.width / 2
                val centerY = size.height / 2
                var startAngle = -90f

                categories.forEach { stat ->
                    val sweepAngle = stat.percent * 360f
                    drawArc(
                        color = stat.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = androidx.compose.ui.geometry.Offset(centerX - radius, centerY - radius),
                    )
                    startAngle += sweepAngle
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(@StringRes title: Int, @StringRes subtitle: Int?) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = stringResource(title), style = MaterialTheme.typography.titleMedium)
        subtitle?.let {
            Text(text = stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class OfferCardState(
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    @param:StringRes val detailRes: Int,
    val accent: Color,
)

private data class CategoryStat(
    @get:StringRes val labelRes: Int,
    val percent: Float,
    val color: Color,
)

private fun getStaticOffers() = listOf(
    OfferCardState(R.string.home_offer_1_title, R.string.home_offer_1_subtitle, R.string.home_offer_1_detail, OfferPeach),
    OfferCardState(R.string.home_offer_2_title, R.string.home_offer_2_subtitle, R.string.home_offer_2_detail, OfferLavender),
    OfferCardState(R.string.home_offer_3_title, R.string.home_offer_3_subtitle, R.string.home_offer_3_detail, OfferSand),
)

private fun initialsFor(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2)
    return if (parts.isEmpty()) "U" else parts.joinToString("") { it.first().uppercase() }
}

private val CelesteBase = Color(0xFF33B2C3)
private val CelesteSoft = Color(0xFF54BDCA)
private val CelesteDeep = Color(0xFF1E8D9B)
private val CelestePastel = Color(0xFF9FE2EA)
private val CelestePale = Color(0xFFD8F4F7)
private val CelesteIce = Color(0xFFE2F7F9)
private val CelesteMist = Color(0xFFF0FBFC)
private val CelesteInk = Color(0xFF1B4B52)
private val PastelGreenDeep = Color(0xFF5FAF9C)
private val PastelGreenPale = Color(0xFFE6F6F1)
private val IndicatorBg = Color(0xFFEEF9FA)
private val IndicatorBorder = Color(0xFFD8F1F3)
private val OfferPeach = Color(0xFFF1B591)
private val OfferLavender = Color(0xFFC7B6E8)
private val OfferSand = Color(0xFFE2C892)
