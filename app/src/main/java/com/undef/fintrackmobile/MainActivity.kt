package com.undef.fintrackmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember

/*
 * 1️⃣ ACTIVITY - Arquitectura Single-Activity
 * MainActivity es el único punto de entrada de la UI.
 * Implementamos 'ComponentActivity' en lugar de AppCompatActivity para optimizar con Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configura la app para usar todo el espacio de pantalla (detrás de status/nav bars)
        enableEdgeToEdge()
        
        // setContent reemplaza los XML tradicionales. Es el puente entre Activity y Compose.
        setContent {
            // 'remember' garantiza que el AppContainer persista durante cambios de configuración
            val container = remember { AppContainer(applicationContext) }
            
            // FintrackApp es el Composable raíz que orquesta toda la aplicación
            FintrackApp(container)
        }
    }
}