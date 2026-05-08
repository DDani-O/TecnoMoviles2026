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
        val lastName = stringPreferencesKey("last_name")
        val email = stringPreferencesKey("email")
        val birthDate = stringPreferencesKey("birth_date")
        val currencyCode = stringPreferencesKey("currency_code")
        val profileImageUri = stringPreferencesKey("profile_image_uri")
        val isLoggedIn = booleanPreferencesKey("is_logged_in")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            displayName = prefs[Keys.displayName] ?: "",
            lastName = prefs[Keys.lastName] ?: "",
            email = prefs[Keys.email] ?: "",
            birthDate = prefs[Keys.birthDate] ?: "",
            currencyCode = prefs[Keys.currencyCode] ?: UserPreferences.DEFAULT.currencyCode,
            profileImageUri = prefs[Keys.profileImageUri],
            isLoggedIn = prefs[Keys.isLoggedIn] ?: UserPreferences.DEFAULT.isLoggedIn
        )
    }

    suspend fun updateDisplayName(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.displayName] = value
        }
    }

    suspend fun updateLastName(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.lastName] = value
        }
    }

    suspend fun updateEmail(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.email] = value
        }
    }

    suspend fun updateBirthDate(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.birthDate] = value
        }
    }

    suspend fun updateCurrencyCode(value: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.currencyCode] = value
        }
    }

    suspend fun updateProfileImageUri(value: String?) {
        context.dataStore.edit { prefs ->
            if (value == null) prefs.remove(Keys.profileImageUri)
            else prefs[Keys.profileImageUri] = value
        }
    }

    suspend fun setLoggedIn(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.isLoggedIn] = value
        }
    }
}
