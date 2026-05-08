package com.undef.fintrackmobile.ui.components.purchase

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * PurchaseTicketSheet: Modal para seleccionar el origen del ticket (Cámara o Galería).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseTicketSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
) {
    val colors = FintrackTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.celesteMist
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.purchase_ticket_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.celesteDeep
            )
            
            Button(
                onClick = onGalleryClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.celesteBase,
                    contentColor = colors.neutralWhite
                )
            ) {
                Icon(imageVector = Icons.Filled.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.purchase_ticket_gallery),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            OutlinedButton(
                onClick = onCameraClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(2.dp, colors.celesteBase)
            ) {
                Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null, tint = colors.celesteBase)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.purchase_ticket_camera),
                    color = colors.celesteBase,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
