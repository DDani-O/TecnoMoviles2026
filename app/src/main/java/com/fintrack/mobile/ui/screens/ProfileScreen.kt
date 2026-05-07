package com.fintrack.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.viewmodel.ProfileViewModel

private data class CurrencyOption(
    val code: String,
    val labelRes: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onOpenSettings: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    var displayName by rememberSaveable { mutableStateOf(preferences.displayName) }
    var lastName by rememberSaveable { mutableStateOf("Pérez") }
    var email by rememberSaveable { mutableStateOf("usuario@email.com") }
    var birthday by rememberSaveable { mutableStateOf("15/06/1995") }
    var address by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var currencyExpanded by rememberSaveable { mutableStateOf(false) }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(preferences.displayName) {
        displayName = preferences.displayName
    }

    val currencies = listOf(
        CurrencyOption("ARS", R.string.currency_ars),
        CurrencyOption("USD", R.string.currency_usd),
    )
    val selectedOption = currencies.firstOrNull { it.code == preferences.currencyCode } ?: currencies.first()

    // Paleta celeste basada en #33B2C3
    val primaryGradientStart = Color(0xFF33B2C3)
    val primaryGradientEnd = Color(0xFF7AD1DC)
    val accentColor = Color(0xFF2B9EAE)
    val titleColor = Color(0xFF1E8D9B)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ENCABEZADO - Título unificado con ExploreScreen
            item {
                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = titleColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // TARJETA DE PERFIL PREMIUM
            item {
                ProfileCardSection(
                    displayName = displayName,
                    lastName = lastName,
                    email = email,
                    birthday = birthday,
                    address = address,
                    city = city,
                    isEditing = isEditing,
                    onDisplayNameChange = { displayName = it },
                    onLastNameChange = { lastName = it },
                    onEmailChange = { email = it },
                    onBirthdayChange = { birthday = it },
                    onAddressChange = { address = it },
                    onCityChange = { city = it },
                    onEditToggle = { isEditing = it },
                    onChangePhoto = {},
                    onSave = {
                        viewModel.updateDisplayName(displayName)
                        isEditing = false
                    },
                    primaryGradientStart = primaryGradientStart,
                    primaryGradientEnd = primaryGradientEnd,
                    accentColor = accentColor,
                )
            }

            // SECCIÓN DE CONFIGURACIONES
            item {
                Text(
                    text = stringResource(R.string.profile_section_prefs),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            // MONEDA
            item {
                SettingCard(
                    title = stringResource(R.string.profile_currency_label),
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = !currencyExpanded },
                    selectedLabel = stringResource(selectedOption.labelRes),
                    options = currencies,
                    onOptionClick = { option ->
                        viewModel.updateCurrencyCode(option.code)
                        currencyExpanded = false
                    }
                )
            }

            // MODO OSCURO
            item {
                SettingToggleCard(
                    title = stringResource(R.string.profile_theme_dark),
                    isChecked = preferences.darkTheme,
                    onToggle = { viewModel.updateDarkTheme(it) }
                )
            }

            // NOTIFICACIONES
            item {
                SettingToggleCard(
                    title = stringResource(R.string.profile_notifications),
                    isChecked = notificationsEnabled,
                    onToggle = { notificationsEnabled = it }
                )
            }

            // CONFIGURACIÓN AVANZADA
            item {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0F7F9),
                        contentColor = titleColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.profile_open_settings),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // CERRAR SESIÓN
            item {
                Button(
                    onClick = {
                        viewModel.logout()
                        onLoggedOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8EDAE3),
                        contentColor = Color(0xFF0F4E57)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.profile_logout),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileCardSection(
    displayName: String,
    lastName: String,
    email: String,
    birthday: String,
    address: String,
    city: String,
    isEditing: Boolean,
    onDisplayNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onBirthdayChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onEditToggle: (Boolean) -> Unit,
    onChangePhoto: () -> Unit,
    onSave: () -> Unit,
    primaryGradientStart: Color,
    primaryGradientEnd: Color,
    accentColor: Color,
) {
    val initials = buildString {
        displayName.trim().firstOrNull()?.let { append(it.uppercaseChar()) }
        lastName.trim().firstOrNull()?.let { append(it.uppercaseChar()) }
    }.ifBlank { "?" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(primaryGradientStart, primaryGradientEnd)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ENCABEZADO CON GRADIENTE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(primaryGradientStart, primaryGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryGradientStart
                            )
                        }
                    }
                    if (isEditing) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clickable(onClick = onChangePhoto),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = stringResource(R.string.profile_photo_action),
                                    tint = primaryGradientEnd,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.profile_section_personal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryGradientStart
                    )
                    TextButton(
                        onClick = { onEditToggle(!isEditing) },
                        colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
                    ) {
                        Text(
                            text = if (isEditing) {
                                stringResource(R.string.profile_cancel)
                            } else {
                                stringResource(R.string.profile_edit)
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (isEditing) {
                    Text(
                        text = stringResource(R.string.profile_photo_action),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // CAMPO: NOMBRE
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { if (isEditing) onDisplayNameChange(it) },
                    label = { Text(stringResource(R.string.profile_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = !isEditing
                )

                // CAMPO: APELLIDO
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { if (isEditing) onLastNameChange(it) },
                    label = { Text(stringResource(R.string.profile_last_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = !isEditing
                )

                // CAMPO: EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (isEditing) onEmailChange(it) },
                    label = { Text(stringResource(R.string.profile_email_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = !isEditing
                )

                // CAMPO: CUMPLEAÑOS
                OutlinedTextField(
                    value = birthday,
                    onValueChange = { if (isEditing) onBirthdayChange(it) },
                    label = { Text(stringResource(R.string.profile_birthday_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = !isEditing
                )

                // CAMPO: DIRECCION
                OutlinedTextField(
                    value = address,
                    onValueChange = { if (isEditing) onAddressChange(it) },
                    label = { Text(stringResource(R.string.profile_address_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = !isEditing
                )

                // CAMPO: CIUDAD
                OutlinedTextField(
                    value = city,
                    onValueChange = { if (isEditing) onCityChange(it) },
                    label = { Text(stringResource(R.string.profile_city_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = !isEditing
                )

                if (isEditing) {
                    // BOTON GUARDAR
                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryGradientStart,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.profile_save),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingCard(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedLabel: String,
    options: List<CurrencyOption>,
    onOptionClick: (CurrencyOption) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange
            ) {
                OutlinedTextField(
                    value = selectedLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(title) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(option.labelRes)) },
                            onClick = { onOptionClick(option) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleCard(
    title: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Switch(
                checked = isChecked,
                onCheckedChange = onToggle
            )
        }
    }
}

