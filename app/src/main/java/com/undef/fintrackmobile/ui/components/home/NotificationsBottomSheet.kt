package com.undef.fintrackmobile.ui.components.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * NotificationsBottomSheet: Panel inferior que muestra las notificaciones del usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val colors = FintrackTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.celesteMist,
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
                color = colors.celesteDeep
            )

            // Notificación estática de ejemplo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.celesteSoft, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.celestePale),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(shape = CircleShape, color = colors.celestePastel, modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Filled.CardGiftcard, contentDescription = null, tint = colors.celesteDeep)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "¡Regalo de bienvenida!", fontWeight = FontWeight.Bold, color = colors.celesteDeep)
                        Text(text = "Tienes un 10% de descuento en tu próxima compra.", color = colors.celesteInk)
                    }
                }
            }
        }
    }
}
