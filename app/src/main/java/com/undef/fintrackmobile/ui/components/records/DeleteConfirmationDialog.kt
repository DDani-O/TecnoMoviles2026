package com.undef.fintrackmobile.ui.components.records

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * DeleteConfirmationDialog: Diálogo para confirmar la eliminación de un registro.
 */
@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = FintrackTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.records_action_delete), fontWeight = FontWeight.Bold) },
        text = { Text(stringResource(R.string.records_delete_confirm)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.pastelRed)
            ) {
                Text(stringResource(R.string.records_action_delete), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.celesteDeep)
            ) {
                Text(stringResource(R.string.records_cancel))
            }
        },
        containerColor = colors.neutralWhite,
        shape = RoundedCornerShape(28.dp)
    )
}
