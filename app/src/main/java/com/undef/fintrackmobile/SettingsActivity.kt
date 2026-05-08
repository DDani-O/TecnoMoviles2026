package com.undef.fintrackmobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.undef.fintrackmobile.ui.screens.SettingsScreen
import com.undef.fintrackmobile.ui.theme.FintrackMobileTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FintrackMobileTheme(dynamicColor = false) {
                // Usamos el nombre del parámetro para evitar confusiones
                SettingsScreen(
                    onBack = { finish() },
                    modifier = Modifier.fillMaxSize(), // Opcional: añade el modificador aquí
                )
            }
        }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}
