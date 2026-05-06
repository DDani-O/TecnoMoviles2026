package com.fintrack.mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.mobile.data.preferences.UserPreferences
import com.fintrack.mobile.data.preferences.UserPreferencesRepository
import com.fintrack.mobile.ui.screens.SettingsScreen
import com.fintrack.mobile.ui.theme.FintrackMobileTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferencesRepository = remember { UserPreferencesRepository(applicationContext) }
            val preferences by preferencesRepository.preferencesFlow.collectAsStateWithLifecycle(
                initialValue = UserPreferences.DEFAULT,
            )
            FintrackMobileTheme(darkTheme = preferences.darkTheme, dynamicColor = false) {
                // Usamos el nombre del parámetro para evitar confusiones
                SettingsScreen(
                    onBack = { finish() },
                    modifier = Modifier.fillMaxSize() // Opcional: añade el modificador aquí
                )
            }
        }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}
