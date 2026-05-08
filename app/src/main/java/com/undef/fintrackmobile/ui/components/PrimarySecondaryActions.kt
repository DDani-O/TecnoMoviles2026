package com.undef.fintrackmobile.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * PrimarySecondaryActions: Columna de botones para acciones principales y secundarias.
 * Implementa el estilo de botones redondeados y colores pastel de la aplicación.
 */
@Composable
fun PrimarySecondaryActions(
    @StringRes primaryLabelRes: Int,
    @StringRes secondaryLabelRes: Int,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    spacing: Dp = 12.dp,
) {
    val colors = FintrackTheme.colors
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Botón Principal
        Button(
            onClick = onPrimary,
            modifier = buttonModifier.height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.celesteDeep,
                contentColor = colors.neutralWhite
            )
        ) {
            Text(
                text = stringResource(primaryLabelRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Botón Secundario
        OutlinedButton(
            onClick = onSecondary,
            modifier = buttonModifier.height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.celesteInk
            ),
            border = BorderStroke(1.dp, colors.celesteSoft)
        ) {
            Text(
                text = stringResource(secondaryLabelRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
