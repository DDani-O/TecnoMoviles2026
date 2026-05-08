package com.fintrack.mobile.ui.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.components.BorderedCard
import com.fintrack.mobile.ui.components.FintrackSectionHeader
import com.fintrack.mobile.ui.components.TitleSubtitleCard
import com.fintrack.mobile.ui.theme.FintrackTheme
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.viewmodel.HomeInsight
import com.fintrack.mobile.ui.viewmodel.InsightCategory
import com.fintrack.mobile.ui.viewmodel.InsightIcon

/**
 * HomeNewsSection: Muestra una lista de insights o noticias relevantes para el usuario.
 */
@Composable
fun HomeNewsSection(
    insights: List<HomeInsight>,
    currencyCode: String,
    totalCents: Long,
    modifier: Modifier = Modifier
) {
    val colors = FintrackTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FintrackSectionHeader(title = R.string.home_news_title, subtitle = R.string.home_news_subtitle)
        if (insights.isEmpty()) {
            TitleSubtitleCard(
                title = stringResource(R.string.home_news_empty_title),
                subtitle = stringResource(R.string.home_news_empty_subtitle),
                containerColor = colors.pastelGreenPale,
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
    val colors = FintrackTheme.colors
    val accent = colors.pastelGreenDeep

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
                            tint = colors.neutralWhite,
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
