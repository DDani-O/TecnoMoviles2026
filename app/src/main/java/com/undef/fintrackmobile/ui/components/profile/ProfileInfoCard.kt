package com.undef.fintrackmobile.ui.components.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.ui.theme.FintrackTheme

/**
 * ProfileInfoCard: Tarjeta principal de información del usuario con diseño degradado.
 * Permite alternar entre modo lectura y edición de datos personales, incluyendo la foto.
 */
@Composable
fun ProfileInfoCard(
    displayName: String,
    lastName: String,
    email: String,
    birthday: String,
    profileImageUri: String?,
    isEditing: Boolean,
    onDisplayNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onBirthdayClick: () -> Unit,
    onChangePhotoClick: () -> Unit,
    onEditToggle: (Boolean) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FintrackTheme.colors
    val initials = buildString {
        displayName.trim().firstOrNull()?.let { append(it.uppercaseChar()) }
        lastName.trim().firstOrNull()?.let { append(it.uppercaseChar()) }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.neutralTransparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(colors.profileGradientStart, colors.profileGradientEnd)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colors.neutralWhite,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cabecera Visual con Avatar y Acción de Foto
                ProfileHeaderBanner(
                    initials = initials,
                    profileImageUri = profileImageUri,
                    isEditing = isEditing,
                    gradientStart = colors.profileGradientStart,
                    gradientEnd = colors.profileGradientEnd,
                    onChangePhotoClick = onChangePhotoClick
                )

                // Sección de Título y Botón de Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.profile_section_personal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.profileGradientStart
                    )
                    TextButton(
                        onClick = { onEditToggle(!isEditing) },
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.profileAccent)
                    ) {
                        Text(
                            text = if (isEditing) stringResource(R.string.profile_cancel) else stringResource(R.string.profile_edit),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Campos de Formulario
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileTextField(
                        value = displayName,
                        onValueChange = onDisplayNameChange,
                        label = stringResource(R.string.profile_name_label),
                        isEditing = isEditing
                    )
                    ProfileTextField(
                        value = lastName,
                        onValueChange = onLastNameChange,
                        label = stringResource(R.string.profile_last_name_label),
                        isEditing = isEditing
                    )
                    ProfileTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = stringResource(R.string.profile_email_label),
                        isEditing = isEditing
                    )
                    
                    ProfileTextField(
                        value = birthday,
                        onValueChange = {},
                        label = stringResource(R.string.profile_birthday_label),
                        isEditing = isEditing,
                        readOnly = true,
                        onClick = onBirthdayClick
                    )
                }

                if (isEditing) {
                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.profileGradientStart,
                            contentColor = colors.neutralWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.profile_save), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderBanner(
    initials: String,
    profileImageUri: String?,
    isEditing: Boolean,
    gradientStart: Color,
    gradientEnd: Color,
    onChangePhotoClick: () -> Unit
) {
    val colors = FintrackTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(gradientStart, gradientEnd))),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(80.dp)
                .clickable(enabled = isEditing) { onChangePhotoClick() },
            shape = CircleShape,
            color = colors.neutralWhite,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (profileImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (initials.isNotBlank()) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = gradientStart
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Pets,
                        contentDescription = null,
                        tint = gradientStart,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                // Overlay de cámara si está editando
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CameraAlt, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isEditing: Boolean,
    readOnly: Boolean = !isEditing,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (isEditing && onClick == null) onValueChange(it) },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            readOnly = readOnly || onClick != null
        )
        
        // Capa transparente para capturar el click si es un selector y estamos editando
        if (isEditing && onClick != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onClick)
            )
        }
    }
}
