package com.undef.fintrackmobile.ui.components.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.viewmodel.OfferItemExplore

/**
 * OfferCardExplore: Tarjeta individual para mostrar detalles de una oferta en la pantalla de exploración.
 */
@Composable
fun OfferCardExplore(
    item: OfferItemExplore,
    modifier: Modifier = Modifier,
) {
    val colors = FintrackTheme.colors
    Card(
        modifier = modifier.width(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.neutralWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.celestePale)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Surface(
                color = colors.pastelGreenPale,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = item.store,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.pastelGreenDeep
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colors.celesteDeep
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.celesteInk.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.explore_offer_cta),
                style = MaterialTheme.typography.labelSmall,
                color = colors.celesteSoft,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
