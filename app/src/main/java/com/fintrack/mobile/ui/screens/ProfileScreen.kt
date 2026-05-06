package com.fintrack.mobile.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.components.LabeledTextField
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
    var lastName by rememberSaveable { mutableStateOf("García") }
    var email by rememberSaveable { mutableStateOf("usuario@gmail.com") }
    var birthday by rememberSaveable { mutableStateOf("15/05/1995") }
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }
    var expandedCurrency by rememberSaveable { mutableStateOf(false) }
    var expandedLanguage by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(preferences.displayName) {
        displayName = preferences.displayName
    }

    val currencies = listOf(
        CurrencyOption("ARS", R.string.currency_ars),
        CurrencyOption("USD", R.string.currency_usd),
    )
    val selectedCurrency = currencies.firstOrNull { it.code == preferences.currencyCode } ?: currencies.first()

    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                // 👤 TARJETA DE PERFIL PREMIUM
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1565C0)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Avatar
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.3f),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = displayName.firstOrNull()?.uppercase() ?: "U",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        // Nombre y Apellido
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$displayName $lastName",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        // Cumpleaños
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "🎂 ",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = birthday,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                            )
                        }

                        // Botón Editar Perfil
                        if (!isEditingProfile) {
                            Button(
                                onClick = { isEditingProfile = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF6B35),
                                ),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(stringResource(R.string.profile_edit_profile))
                            }
                        }
                    }
                }
            }

            // 📝 FORMULARIO DE EDICIÓN (EXPANDIBLE)
            if (isEditingProfile) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                color = Color(0xFF1565C0).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LabeledTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            labelRes = R.string.profile_name_label,
                            placeholderRes = R.string.placeholder_name,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        LabeledTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            labelRes = R.string.profile_lastname_label,
                            placeholderRes = R.string.placeholder_name,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        LabeledTextField(
                            value = email,
                            onValueChange = { email = it },
                            labelRes = R.string.profile_email_label,
                            placeholderRes = R.string.placeholder_email,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        LabeledTextField(
                            value = birthday,
                            onValueChange = { birthday = it },
                            labelRes = R.string.profile_birthday_label,
                            placeholderRes = R.string.placeholder_name,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = { isEditingProfile = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1565C0),
                                ),
                            ) {
                                Text(stringResource(R.string.profile_save))
                            }
                            Button(
                                onClick = { isEditingProfile = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.LightGray,
                                ),
                            ) {
                                Text("Cancelar", color = Color.Black)
                            }
                        }
                    }
                }
            }

            // ⚙️ CONFIGURACIÓN DEL SISTEMA
            item {
                Text(
                    text = stringResource(R.string.profile_section_settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            // Moneda
            item {
                SettingCard(
                    title = stringResource(R.string.profile_currency_label),
                    icon = "💱",
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedCurrency,
                        onExpandedChange = { expandedCurrency = !expandedCurrency },
                    ) {
                        OutlinedTextField(
                            value = stringResource(selectedCurrency.labelRes),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.profile_currency_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCurrency) },
                            modifier = Modifier
                                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCurrency,
                            onDismissRequest = { expandedCurrency = false },
                        ) {
                            currencies.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(option.labelRes)) },
                                    onClick = {
                                        viewModel.updateCurrencyCode(option.code)
                                        expandedCurrency = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Idioma
            item {
                SettingCard(
                    title = stringResource(R.string.profile_language_label),
                    icon = "🌐",
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedLanguage,
                        onExpandedChange = { expandedLanguage = !expandedLanguage },
                    ) {
                        OutlinedTextField(
                            value = "Español",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.profile_language_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguage) },
                            modifier = Modifier
                                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = expandedLanguage,
                            onDismissRequest = { expandedLanguage = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.profile_language_spanish)) },
                                onClick = { expandedLanguage = false },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.profile_language_english)) },
                                onClick = { expandedLanguage = false },
                            )
                        }
                    }
                }
            }

            // Modo Oscuro
            item {
                SettingCard(
                    title = stringResource(R.string.profile_theme_dark),
                    icon = "🌙",
                ) {
                    Switch(
                        checked = preferences.darkTheme,
                        onCheckedChange = { viewModel.updateDarkTheme(it) },
                    )
                }
            }

            // Borrar Caché
            item {
                SettingCard(
                    title = stringResource(R.string.profile_clear_cache),
                    icon = "🗑️",
                ) {
                    Button(
                        onClick = { /* TODO: Implementar borrar caché */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B35),
                        ),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text("Borrar")
                    }
                }
            }

            // Acerca de
            item {
                SettingCard(
                    title = stringResource(R.string.profile_about),
                    icon = "ℹ️",
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.settings_about_text),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Cerrar Sesión
            item {
                Button(
                    onClick = onLoggedOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF5350),
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        Icons.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.profile_logout))
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    icon: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            content()
        }
    }
}
            Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.profile_open_settings))
            }
            Button(
                onClick = {
                    viewModel.logout()
                    onLoggedOut()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.profile_logout))
            }
        }
    }
}
