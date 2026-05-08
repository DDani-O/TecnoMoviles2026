package com.fintrack.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fintrack.mobile.BuildConfig
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.components.profile.SettingDropdownCard
import com.fintrack.mobile.ui.components.profile.SettingToggleCard
import com.fintrack.mobile.ui.theme.FintrackTheme

/**
 * SettingsScreen: Pantalla de configuraciones generales de la aplicación.
 * Incluye opciones de personalización, privacidad, mantenimiento e información legal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = FintrackTheme.colors
    val scrollState = rememberScrollState()
    
    // Estados locales para simular configuraciones
    var languageExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedLanguage by rememberSaveable { mutableStateOf("Español") }
    val languages = listOf("Español", "English", "Português")

    var notificationSound by rememberSaveable { mutableStateOf(true) }
    var biometricEnabled by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        color = colors.celesteDeep
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = colors.celesteDeep
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.celesteMist
                )
            )
        },
        containerColor = colors.celesteMist
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // SECCIÓN: PERSONALIZACIÓN
            SettingsGroup(title = "Sonido y Notificaciones", icon = Icons.Filled.Notifications) {
                SettingToggleCard(
                    title = "Sonidos de Notificación",
                    isChecked = notificationSound,
                    onToggle = { notificationSound = it }
                )
            }

            // SECCIÓN: SEGURIDAD Y PRIVACIDAD
            SettingsGroup(title = "Seguridad", icon = Icons.Filled.Security) {
                SettingToggleCard(
                    title = "Bloqueo Biométrico",
                    isChecked = biometricEnabled,
                    onToggle = { biometricEnabled = it }
                )
                SettingsActionCard(
                    title = "Privacidad y Datos",
                    subtitle = "Gestiona qué datos compartes",
                    icon = Icons.Filled.PrivacyTip,
                    iconColor = colors.celesteDeep,
                    onClick = {}
                )
            }

            // SECCIÓN: IDIOMA
            SettingsGroup(title = "Internacionalización", icon = Icons.Filled.Language) {
                SettingDropdownCard(
                    title = stringResource(R.string.profile_language),
                    expanded = languageExpanded,
                    onExpandedChange = { languageExpanded = it },
                    selectedLabel = selectedLanguage,
                    options = languages,
                    optionLabel = { it },
                    onOptionClick = { 
                        selectedLanguage = it
                        languageExpanded = false
                    }
                )
            }

            // SECCIÓN: SISTEMA Y LIMPIEZA
            SettingsGroup(title = "Mantenimiento", icon = Icons.Filled.Cached) {
                SettingsActionCard(
                    title = stringResource(R.string.profile_clear_cache),
                    subtitle = stringResource(R.string.profile_clear_cache_desc),
                    icon = Icons.Filled.Cached,
                    iconColor = colors.pastelAmber,
                    onClick = {}
                )
                SettingsActionCard(
                    title = "Exportar Datos",
                    subtitle = "Descarga tus gastos en formato CSV",
                    icon = Icons.Filled.Download,
                    iconColor = colors.pastelGreenDeep,
                    onClick = {}
                )
            }

            // SECCIÓN: AYUDA Y SOPORTE
            SettingsGroup(title = "Soporte", icon = Icons.AutoMirrored.Filled.HelpCenter) {
                SettingsActionCard(
                    title = "Centro de Ayuda",
                    subtitle = "Preguntas frecuentes y tutoriales",
                    icon = Icons.Filled.QuestionAnswer,
                    iconColor = colors.celesteDeep,
                    onClick = {}
                )
                SettingsActionCard(
                    title = "Contactar Soporte",
                    subtitle = "Escríbenos si tienes problemas",
                    icon = Icons.Filled.Email,
                    iconColor = colors.celesteDeep,
                    onClick = {}
                )
            }

            // SECCIÓN: ACERCA DE
            SettingsGroup(title = "Información Legal", icon = Icons.Filled.Info) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = colors.celesteIce.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.settings_about_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.settings_about_text),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider(color = colors.celesteDeep.copy(alpha = 0.1f))
                        Text(
                            text = stringResource(R.string.settings_version_label, BuildConfig.VERSION_NAME),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.celesteDeep
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = FintrackTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(icon, null, tint = colors.celesteDeep, modifier = Modifier.size(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.celesteDeep
            )
        }
        content()
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val colors = FintrackTheme.colors
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colors.neutralWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.celesteSoft
            )
        }
    }
}
