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

/*
 * 4️⃣ INTENTS - SettingsActivity (Activity Independiente)
 * Esta Activity demuestra cuándo es necesario salir del patrón Single-Activity.
 * Settings tiene su propio ciclo de vida y backstack manejado por Android.
 */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FintrackMobileTheme(dynamicColor = false) {
                SettingsScreen(
                    // finish() cierra esta Activity y nos devuelve a MainActivity en el backstack
                    onBack = { finish() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    companion object {
        /**
         * Factory method que encapsula la creación del Intent.
         * Proporciona Type-Safety al asegurar que el Intent se cree correctamente.
         */
        fun intent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}
