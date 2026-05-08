package com.undef.fintrackmobile.ui.components.purchase

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * PurchaseActionButtons: Fila de botones para agregar productos manualmente o cargar un ticket.
 */
@Composable
fun PurchaseActionButtons(
    onAddProduct: () -> Unit,
    onLoadTicket: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FintrackTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onAddProduct,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.celesteBase,
                contentColor = colors.neutralWhite
            ),
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(text = stringResource(R.string.purchase_load_product))
        }
        
        OutlinedButton(
            onClick = onLoadTicket,
            border = BorderStroke(2.dp, colors.celesteBase),
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = stringResource(R.string.purchase_load_ticket),
                color = colors.celesteBase
            )
        }
    }
}
