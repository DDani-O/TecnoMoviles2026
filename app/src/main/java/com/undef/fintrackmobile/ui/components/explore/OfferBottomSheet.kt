package com.undef.fintrackmobile.ui.components.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme
import com.undef.fintrackmobile.ui.viewmodel.OfferItemExplore

/**
 * OfferBottomSheet: Muestra el detalle completo de una oferta en una hoja modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferBottomSheet(
    offer: OfferItemExplore,
    onDismiss: () -> Unit,
    sheetState: SheetState,
) {
    val colors = FintrackTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.neutralWhite,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(colors.celestePale)
            )
        }
    ) {
        OfferDetailContent(offer = offer)
    }
}

@Composable
private fun OfferDetailContent(offer: OfferItemExplore) {
    val colors = FintrackTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Cabecera con Icono
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = colors.pastelGreenPale
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = colors.pastelGreenDeep,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Column {
                Text(
                    text = offer.store,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.pastelGreenDeep
                )
                Text(
                    text = stringResource(R.string.explore_offer_tag),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.celesteInk.copy(alpha = 0.5f)
                )
            }
        }

        // Título y Descripción completa
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = offer.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = colors.celesteDeep
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.celesteMist.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.celestePale)
            ) {
                Text(
                    text = offer.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.celesteInk,
                    modifier = Modifier.padding(20.dp),
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                )
            }
        }

        // Información adicional (simulada o futura)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                tint = colors.celesteDeep,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.explore_available_store, offer.store),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.celesteInk.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* Acción futura: Ver productos o ir a tienda */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.celesteDeep)
        ) {
            Text(
                text = stringResource(R.string.explore_offer_cta_full),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
