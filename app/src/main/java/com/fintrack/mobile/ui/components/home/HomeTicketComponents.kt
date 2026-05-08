package com.fintrack.mobile.ui.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fintrack.mobile.R
import com.fintrack.mobile.data.local.entity.PurchaseWithProducts
import com.fintrack.mobile.ui.theme.FintrackTheme
import com.fintrack.mobile.ui.util.formatCurrency
import com.fintrack.mobile.ui.util.formatDate
import com.fintrack.mobile.ui.util.getSupermarketColors

/**
 * HomeTicketCarousel: Carrusel que muestra las últimas compras realizadas en formato de ticket.
 */
@Composable
fun HomeTicketCarousel(
    purchases: List<PurchaseWithProducts>,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
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
    val brandColors = FintrackTheme.colors
    val (bgColor, accentColor, logoRes) = getSupermarketColors(purchase.purchase.supermarketName)

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
                            color = brandColors.neutralBlack
                        )
                        Text(
                            text = "PRECIO UNITARIO: ${formatCurrency(product.priceCents, currencyCode)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = brandColors.neutralDarkGray
                        )
                        Text(
                            text = "CANTIDAD: ${product.quantity}",
                            style = MaterialTheme.typography.labelMedium,
                            color = brandColors.neutralDarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(0.3f),
                            thickness = 0.5.dp,
                            color = brandColors.neutralLightGray
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
                    Text(text = stringResource(R.string.home_ticket_total), color = brandColors.neutralWhite, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatCurrency(purchase.purchase.totalCents, currencyCode),
                        color = brandColors.neutralWhite,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
