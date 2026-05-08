package com.undef.fintrackmobile.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.undef.fintrackmobile.R

/*
 * 3️⃣ NAVIGATION COMPOSE - Rutas y Destinos
 * Usamos Sealed Classes para definir destinos de navegación de forma Type-Safe.
 * Esto garantiza que el compilador verifique las rutas y nos permite asociar metadatos (iconos, labels).
 */
sealed class FintrackDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    data object Home : FintrackDestination("home", R.string.nav_home, Icons.Filled.Home)
    data object Explore : FintrackDestination("explore", R.string.nav_explore, Icons.Filled.Search)
    data object NewPurchase : FintrackDestination("new_purchase", R.string.nav_new_purchase, Icons.Filled.AddCircle)
    data object Records : FintrackDestination("records", R.string.nav_records, Icons.AutoMirrored.Filled.ReceiptLong)
    data object Profile : FintrackDestination("profile", R.string.nav_profile, Icons.Filled.Person)

    companion object {
        val bottomItems = listOf(Home, Explore, NewPurchase, Records, Profile)
    }
}

// Constantes para rutas que no están en la barra de navegación inferior
object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ADJUST_TICKET = "adjust_ticket"
}
