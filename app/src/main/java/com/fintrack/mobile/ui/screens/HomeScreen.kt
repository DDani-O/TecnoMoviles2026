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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
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
import com.fintrack.mobile.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

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
    val purchases by viewModel.recentPurchases.collectAsStateWithLifecycle()
    val purchasesWithProducts by viewModel.recentPurchasesWithProducts.collectAsStateWithLifecycle()
    val monthlyTotal by viewModel.monthlyTotalCents.collectAsStateWithLifecycle()
    val greetingName = displayName.ifBlank { stringResource(R.string.default_user_name) }
    val monthAverage = if (purchases.isEmpty()) 0L else purchases.sumOf { it.totalCents } / purchases.size
    val biggestPurchase = purchases.maxByOrNull { it.totalCents }

    var showNotificationsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val weeklyStats = remember(purchases) {
        val now = System.currentTimeMillis()
        val weekMillis = 7 * 24 * 60 * 60 * 1000L
        val realStats = (0..3).map { weekIndex ->
            val end = now - (weekIndex * weekMillis)
            val start = end - weekMillis
            purchases.filter { it.dateMillis in start until end }.sumOf { it.totalCents }
        }.reversed()
        
        if (realStats.all { it == 0L } || realStats.count { it > 0 } < 2) {
            listOf(145000L, 0L, 210000L, 385000L) 
        } else {
            realStats
        }
    }

    val supermarketMonthlyStats = remember(purchases) {
        purchases.groupBy { it.supermarketName }
            .mapValues { entry -> entry.value.sumOf { it.totalCents } }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
    }

    val habitInsight = if (purchasesWithProducts.size >= 2) {
        val history = purchasesWithProducts.drop(1)
        val avgItems = history.sumOf { it.products.size }.toFloat() / history.size
        val currentItems = purchasesWithProducts.first().products.size
        if (currentItems > avgItems * 1.2f && currentItems > 2) {
            val percent = (((currentItems - avgItems) / avgItems) * 100).toInt()
            HomeInsight(
                title = stringResource(R.string.home_news_habit_change_title),
                subtitle = stringResource(R.string.home_news_habit_change_value, currentItems, percent),
                icon = Icons.Filled.ShoppingCart,
                accent = PastelGreenDeep,
            )
        } else {
            HomeInsight(
                title = stringResource(R.string.home_news_habit_change_title),
                subtitle = stringResource(R.string.home_news_habit_change_value, 12, 35),
                icon = Icons.Filled.ShoppingCart,
                accent = PastelGreenDeep,
            )
        }
    } else {
        HomeInsight(
            title = stringResource(R.string.home_news_habit_change_title),
            subtitle = stringResource(R.string.home_news_habit_change_value, 12, 35),
            icon = Icons.Filled.ShoppingCart,
            accent = PastelGreenDeep,
        )
    }

    val insights = listOfNotNull(
        habitInsight,
        if (monthlyTotal > 0L) {
            HomeInsight(
                title = stringResource(R.string.home_news_spending_title),
                subtitle = stringResource(R.string.home_news_spending_value, formatCurrency(monthlyTotal, currencyCode)),
                icon = Icons.Filled.TrendingUp,
                accent = PastelGreenDeep,
            )
        } else null,
        if (purchases.isNotEmpty()) {
            HomeInsight(
                title = stringResource(R.string.home_news_recent_title),
                subtitle = stringResource(R.string.home_news_recent_value, purchases.first().supermarketName),
                icon = Icons.Filled.Storefront,
                accent = PastelGreenDeep,
            )
        } else null,
        if (purchases.size >= 2) {
            val repeatedStore = purchases.groupingBy { it.supermarketName }
                .eachCount()
                .maxByOrNull { it.value }
            val storeCount = repeatedStore?.value ?: 0
            if (storeCount >= 2) {
                HomeInsight(
                    title = stringResource(R.string.home_news_loyalty_title),
                    subtitle = stringResource(R.string.home_news_loyalty_value, storeCount, repeatedStore?.key ?: ""),
                    icon = Icons.Filled.ReceiptLong,
                    accent = PastelGreenDeep,
                )
            } else null
        } else null,
    )
    val offers = listOf(
        OfferCardState(
            title = stringResource(R.string.home_offer_1_title),
            subtitle = stringResource(R.string.home_offer_1_subtitle),
            detail = stringResource(R.string.home_offer_1_detail),
            accent = OfferPeach,
        ),
        OfferCardState(
            title = stringResource(R.string.home_offer_2_title),
            subtitle = stringResource(R.string.home_offer_2_subtitle),
            detail = stringResource(R.string.home_offer_2_detail),
            accent = OfferLavender,
        ),
        OfferCardState(
            title = stringResource(R.string.home_offer_3_title),
            subtitle = stringResource(R.string.home_offer_3_subtitle),
            detail = stringResource(R.string.home_offer_3_detail),
            accent = OfferSand,
        ),
    )
    val historyStats = listOf(
        HistoryStat(
            title = stringResource(R.string.home_stats_month_total),
            value = formatCurrency(monthlyTotal, currencyCode),
            subtitle = stringResource(R.string.home_stats_month_total_subtitle),
        ),
        HistoryStat(
            title = stringResource(R.string.home_stats_avg_ticket),
            value = formatCurrency(monthAverage, currencyCode),
            subtitle = stringResource(R.string.home_stats_avg_ticket_subtitle),
        ),
        HistoryStat(
            title = stringResource(R.string.home_stats_biggest_ticket),
            value = biggestPurchase?.let { formatCurrency(it.totalCents, currencyCode) } ?: stringResource(R.string.home_stats_no_data),
            subtitle = biggestPurchase?.supermarketName ?: stringResource(R.string.home_stats_empty_subtitle),
        ),
        HistoryStat(
            title = stringResource(R.string.home_stats_total_purchases),
            value = purchases.size.toString(),
            subtitle = stringResource(R.string.home_stats_total_purchases_subtitle),
        ),
    )
    val offerListState = rememberLazyListState()
    var offerIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(offers.size) {
        if (offers.isNotEmpty()) {
            while (true) {
                delay(3200)
                offerIndex = (offerIndex + 1) % offers.size
                offerListState.animateScrollToItem(offerIndex)
            }
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                HeaderRow(
                    displayName = greetingName,
                    profileImagePainter = profileImagePainter,
                    onNotificationsClick = { showNotificationsSheet = true },
                    onProfileClick = onProfileClick,
                    notificationCount = 1,
                )
            }

            item {
                NewsFeedSection(insights = insights)
            }

            item {
                SummaryCard(
                    totalFormatted = formatCurrency(monthlyTotal, currencyCode),
                    averageFormatted = formatCurrency(monthAverage, currencyCode),
                    weeklyStats = weeklyStats,
                    currencyCode = currencyCode,
                )
            }

            item {
                SectionHeader(title = R.string.home_stats_history_title, subtitle = R.string.home_stats_history_subtitle)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(historyStats, key = { it.title }) { stat ->
                        HistoryStatCard(stat = stat)
                    }
                }
            }

            item {
                SectionHeader(title = R.string.home_offers_title, subtitle = R.string.home_offers_subtitle)
                Spacer(modifier = Modifier.height(12.dp))
                OfferCarousel(items = offers, state = offerListState)
            }

            item {
                SectionHeader(title = R.string.home_recent_history, subtitle = R.string.home_recent_history_subtitle)
                Spacer(modifier = Modifier.height(12.dp))
                TicketCarousel(purchases = purchasesWithProducts.take(4), currencyCode = currencyCode)
            }

            item {
                SectionHeader(title = R.string.home_expenses_category, subtitle = null)
                CategorySection()
            }

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
                        SupermarketMonthlySpendingSection(stats = supermarketMonthlyStats, currencyCode = currencyCode)
                    }
                }
            }

        }
    }

    if (showNotificationsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationsSheet = false },
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
                    text = "Notificaciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CelesteDeep
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CelesteSoft, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = CelestePastel
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CelestePale,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.CardGiftcard,
                                    contentDescription = null,
                                    tint = CelesteDeep,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "¡Regalo de bienvenida!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CelesteDeep
                            )
                            Text(
                                text = "Tienes un 10% de descuento en tu próxima compra por crear tu cuenta.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CelesteInk
                            )
                        }
                    }
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
    notificationCount: Int,
) {
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
                if (notificationCount > 0) {
                    Badge(
                        modifier = Modifier.padding(4.dp),
                        containerColor = CelesteDeep
                    ) {
                        Text(text = notificationCount.toString(), color = Color.White)
                    }
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
                    InsightCard(insight = insight)
                }
            }
        }
    }
}

@Composable
private fun InsightCard(insight: HomeInsight) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = insight.accent.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = insight.accent.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(16.dp),
    ) {
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
                color = insight.accent,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = insight.icon,
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
                    text = insight.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = insight.accent,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = insight.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
        items(items, key = { it.title }) { offer ->
            OfferCard(offer = offer)
        }
    }
}

@Composable
private fun OfferCard(offer: OfferCardState) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.84f)
            .border(
                width = 1.dp,
                color = offer.accent.copy(alpha = 0.5f),
                shape = RoundedCornerShape(22.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = offer.accent.copy(alpha = 0.16f)),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = offer.title, style = MaterialTheme.typography.titleMedium)
            Text(text = offer.subtitle, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = offer.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
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
    // 1. Configuración de colores y logos (Sección 5 .md)
    val (bgColor, accentColor, logoRes) = when {
        purchase.purchase.supermarketName.contains("Carrefour", ignoreCase = true) ->
            Triple(Color(0xFFEEF6FF), Color(0xFF5FA8E6), R.drawable.logo_carrefour)
        purchase.purchase.supermarketName.contains("Coto", ignoreCase = true) ->
            Triple(Color(0xFFFFF1F1), Color(0xFFEB8A8A), R.drawable.logo_coto)
        purchase.purchase.supermarketName.contains("Jumbo", ignoreCase = true) ->
            Triple(Color(0xFFEDF8EF), Color(0xFF7BCB85), R.drawable.logo_jumbo)
        else ->
            Triple(Color(0xFFF6FAFB), Color(0xFF7A8C93), null)
    }

    Card(
        modifier = Modifier
            .width(320.dp) // Ancho fijo para el carrusel
            .height(450.dp) // ALTURA FIJA PARA TODAS LAS TARJETAS
            .padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // CABECERA: Logo y Supermercado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (logoRes != null) {
                        Image(
                            painter = painterResource(id = logoRes),
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

            // SECCIÓN DE PRODUCTOS CON SCROLL
            // El weight(1f) hace que esta sección use todo el espacio disponible entre la cabecera y el total
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // Permite scroll si hay muchos productos
            ) {
                purchase.products.forEach { product ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        // NOMBRE PRODUCTO
                        Text(
                            text = product.name.uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        // PRECIO UNITARIO
                        Text(
                            text = "PRECIO UNITARIO: ${formatCurrency(product.priceCents, currencyCode)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.DarkGray
                        )
                        // CANTIDAD
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

            // TOTAL FINAL (Fijo en la base)
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
                    Text(
                        text = "TOTAL",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
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

// Helpers Adicionales
private fun initialsFor(name: String): String {
    val parts = name.trim().split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
    return if (parts.isEmpty()) "U" else parts.joinToString("") { it.first().uppercase() }
}

@Composable
private fun HistoryStatCard(stat: HistoryStat) {
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
            Text(text = stat.title, style = MaterialTheme.typography.labelMedium)
            Text(text = stat.value, style = MaterialTheme.typography.titleLarge)
            Text(
                text = stat.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


private data class HomeInsight(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accent: Color,
)

private data class OfferCardState(
    val title: String,
    val subtitle: String,
    val detail: String,
    val accent: Color,
)

private data class HistoryStat(
    val title: String,
    val value: String,
    val subtitle: String,
)

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
private val OrangePastel = Color(0xFFFFE3C7)
private val IndicatorBg = Color(0xFFEEF9FA)
private val IndicatorBorder = Color(0xFFD8F1F3)
private val OrangePale = Color(0xFFFFF4E8)
private val OfferPeach = Color(0xFFF1B591)
private val OfferLavender = Color(0xFFC7B6E8)
private val OfferSand = Color(0xFFE2C892)

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
                Column(modifier = Modifier.weight(0.42f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryMetric(
                        label = stringResource(R.string.home_month_total_label),
                        value = totalFormatted,
                        emphasized = true
                    )
                    SummaryMetric(
                        label = stringResource(R.string.home_month_average_label),
                        value = averageFormatted,
                    )
                }
                Box(modifier = Modifier.weight(0.58f)) {
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
private fun SummaryMetric(
    label: String,
    value: String,
    emphasized: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = CelesteDeep.copy(alpha = 0.75f))
        Text(
            text = value,
            style = if (emphasized) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = if (emphasized) CelesteDeep else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BarChart(
    values: List<Float>,
    labels: List<String>,
    amounts: List<String>,
) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            values.forEachIndexed { index, value ->
                val ratio = value / maxValue
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = amounts[index],
                        style = MaterialTheme.typography.labelSmall,
                        color = CelesteBase,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((ratio * 80).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(CelesteBase),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = labels[index],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SupermarketMonthlySpendingSection(
    stats: List<Pair<String, Long>>,
    currencyCode: String,
) {
    val maxSpent = stats.maxOfOrNull { it.second }?.toFloat()?.coerceAtLeast(1f) ?: 1f
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        stats.forEach { (name, total) ->
            val ratio = total.toFloat() / maxSpent
            val barColor = when {
                name.contains("Carrefour", ignoreCase = true) -> Color(0xFF5FA8E6)
                name.contains("Coto", ignoreCase = true) -> Color(0xFFEB8A8A)
                name.contains("Jumbo", ignoreCase = true) -> Color(0xFF7BCB85)
                else -> CelesteDeep
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = formatCurrency(total, currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        color = barColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(barColor.copy(alpha = 0.18f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(barColor)
                    )
                }
            }
        }
    }
}

private data class CategoryStat(
    @get:StringRes val labelRes: Int,
    val percent: Float,
    val color: Color,
)

@Composable
private fun CategorySection() {
    val categories = listOf(
        CategoryStat(R.string.category_food, 0.45f, Color(0xFF7BB9F1)),
        CategoryStat(R.string.category_cleaning, 0.2f, Color(0xFFFFD48A)),
        CategoryStat(R.string.category_home, 0.15f, Color(0xFFF3A1A1)),
        CategoryStat(R.string.category_other, 0.2f, Color(0xFFD1A6E8)),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(0.55f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { stat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(16.dp),
                        shape = CircleShape,
                        color = stat.color,
                    ) {}
                    Text(
                        text = stringResource(stat.labelRes),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${(stat.percent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(0.45f)
                .height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
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
                        topLeft = androidx.compose.ui.geometry.Offset(
                            centerX - radius,
                            centerY - radius,
                        ),
                    )
                    startAngle += sweepAngle
                }
            }
        }
    }
}


@Composable
private fun SectionHeader(
    @StringRes title: Int,
    @StringRes subtitle: Int?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = stringResource(title), style = MaterialTheme.typography.titleMedium)
        subtitle?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}