package com.fintrack.mobile.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "fintrack_prefs")

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val displayName = stringPreferencesKey("display_name")
        val email = stringPreferencesKey("email")
        val currencyCode = stringPreferencesKey("currency_code")
        val darkTheme = booleanPreferencesKey("dark_theme")
        val isLoggedIn = booleanPreferencesKey("is_logged_in")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            displayName = prefs[Keys.displayName] ?: "",
            email = prefs[Keys.email] ?: "",
            currencyCode = prefs[Keys.currencyCode] ?: UserPreferences.DEFAULT.currencyCode,
            darkTheme = prefs[Keys.darkTheme] ?: UserPreferences.DEFAULT.darkTheme,
            isLoggedIn = prefs[Keys.isLoggedIn] ?: UserPreferences.DEFAULT.isLoggedIn
        )
    }

    suspend fun updateDisplayName(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.displayName] = value
        }
    }

    suspend fun updateEmail(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.email] = value
        }
    }

    suspend fun updateCurrencyCode(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.currencyCode] = value
        }
    }

    suspend fun updateDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.darkTheme] = enabled
        }
    }

    suspend fun setLoggedIn(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.isLoggedIn] = value
        }
    }
}
