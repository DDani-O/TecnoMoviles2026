package com.undef.fintrackmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.fintrackmobile.data.network.NetworkModule
import com.undef.fintrackmobile.data.preferences.UserPreferences
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppStateViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences.DEFAULT)

    init {
        viewModelScope.launch {
            repository.preferencesFlow.collect { prefs ->
                if (prefs.isLoggedIn && prefs.accessToken.isNotBlank()) {
                    NetworkModule.setAuthToken(prefs.accessToken)
                    
                    // Verificación simple de expiración (opcional pero recomendada)
                    if (isTokenExpired(prefs.accessToken)) {
                        android.util.Log.w("AppStateVM", "Token detected as expired on startup. Logging out.")
                        logout()
                    }
                } else {
                    NetworkModule.setAuthToken(null)
                }
            }
        }
    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return true
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
            val json = org.json.JSONObject(payload)
            val exp = json.optLong("exp", 0)
            val now = System.currentTimeMillis() / 1000
            exp < now
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.setLoggedIn(false)
            repository.updateAccessToken("")
            repository.updateUserId("")
            NetworkModule.setAuthToken(null)
        }
    }
}
