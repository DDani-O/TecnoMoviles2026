package com.fintrack.mobile.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.R
import com.fintrack.mobile.ui.components.FintrackSectionHeader
import com.fintrack.mobile.ui.components.ImagePickerSheet
import com.fintrack.mobile.ui.components.profile.ProfileInfoCard
import com.fintrack.mobile.ui.components.profile.SettingDropdownCard
import com.fintrack.mobile.ui.components.profile.SettingToggleCard
import com.fintrack.mobile.ui.theme.FintrackTheme
import com.fintrack.mobile.ui.util.formatDate
import com.fintrack.mobile.ui.util.showDatePicker
import com.fintrack.mobile.ui.viewmodel.ProfileViewModel
import java.io.File

private data class CurrencyOption(
    val code: String,
    val labelRes: Int,
)

/**
 * ProfileScreen: Pantalla de gestión de perfil de usuario y configuraciones de la app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onOpenSettings: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val colors = FintrackTheme.colors

    // Estados locales para edición (State Hoisting)
    var displayName by rememberSaveable(preferences.displayName) { mutableStateOf(preferences.displayName) }
    var lastName by rememberSaveable(preferences.lastName) { mutableStateOf(preferences.lastName) }
    var email by rememberSaveable(preferences.email) { mutableStateOf(preferences.email) }
    var birthDate by rememberSaveable(preferences.birthDate) { mutableStateOf(preferences.birthDate) }
    
    val showPhotoSheetState = rememberSaveable { mutableStateOf(value = false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val photoSheetState = rememberModalBottomSheetState()
    
    var isCurrencyExpanded by rememberSaveable { mutableStateOf(value = false) }
    var isEditing by rememberSaveable { mutableStateOf(value = false) }
    var areNotificationsEnabled by rememberSaveable { mutableStateOf(true) }

    val currencies = remember {
        listOf(
            CurrencyOption("ARS", R.string.currency_ars),
            CurrencyOption("USD", R.string.currency_usd),
        )
    }
    val selectedOption = currencies.firstOrNull { it.code == preferences.currencyCode } ?: currencies.first()

    // Launchers para Imagen
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.updateProfileImage(it.toString()) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCameraUri?.let { viewModel.updateProfileImage(it.toString()) }
        pendingCameraUri = null
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createProfileImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Título Principal
            item {
                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.celesteInk,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            // TARJETA DE PERFIL
            item {
                ProfileInfoCard(
                    displayName = displayName,
                    lastName = lastName,
                    email = email,
                    birthday = birthDate,
                    profileImageUri = preferences.profileImageUri,
                    isEditing = isEditing,
                    onDisplayNameChange = { displayName = it },
                    onLastNameChange = { lastName = it },
                    onEmailChange = { email = it },
                    onBirthdayClick = {
                        showDatePicker(context, System.currentTimeMillis()) {
                            birthDate = formatDate(it)
                        }
                    },
                    onChangePhotoClick = { showPhotoSheetState.value = true },
                    onEditToggle = { isEditing = it },
                    onSave = {
                        viewModel.updateDisplayName(displayName)
                        viewModel.updatePersonalData(lastName, email, birthDate)
                        isEditing = false
                    }
                )
            }

            // SECCIÓN DE PREFERENCIAS
            item {
                FintrackSectionHeader(
                    title = R.string.profile_section_prefs,
                    subtitle = "Personaliza tu experiencia en Fintrack"
                )
            }

            // Configuración: Moneda
            item {
                SettingDropdownCard(
                    title = stringResource(R.string.profile_currency_label),
                    expanded = isCurrencyExpanded,
                    onExpandedChange = { isCurrencyExpanded = it },
                    selectedLabel = stringResource(selectedOption.labelRes),
                    options = currencies,
                    optionLabel = { stringResource(it.labelRes) },
                    onOptionClick = { option ->
                        viewModel.updateCurrencyCode(option.code)
                        isCurrencyExpanded = false
                    }
                )
            }

            // Configuración: Notificaciones
            item {
                SettingToggleCard(
                    title = stringResource(R.string.profile_notifications),
                    isChecked = areNotificationsEnabled,
                    onToggle = { areNotificationsEnabled = it }
                )
            }

            // CONFIGURACIÓN AVANZADA
            item {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.celesteIce,
                        contentColor = colors.celesteInk,
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.profileLogoutContainer,
                        contentColor = colors.profileLogoutContent
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
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

    if (showPhotoSheetState.value) {
        ImagePickerSheet(
            title = stringResource(R.string.profile_photo_action),
            sheetState = photoSheetState,
            onDismiss = { showPhotoSheetState.value = false },
            onGalleryClick = {
                showPhotoSheetState.value = false
                galleryLauncher.launch("image/*")
            }
        ) {
            showPhotoSheetState.value = false
            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                val uri = createProfileImageUri(context)
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

private fun createProfileImageUri(context: Context): Uri {
    val dir = File(context.cacheDir, "profile").apply { if (!exists()) mkdirs() }
    val file = File(dir, "profile_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
