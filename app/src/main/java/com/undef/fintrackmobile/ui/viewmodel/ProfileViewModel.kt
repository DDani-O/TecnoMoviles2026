package com.undef.fintrackmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.fintrackmobile.data.preferences.UserPreferences
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
import com.undef.fintrackmobile.data.repository.SincronizacionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: UserPreferencesRepository,
    private val sincronizacionRepository: SincronizacionRepository
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences.DEFAULT)

    fun updateDisplayName(value: String) {
        viewModelScope.launch {
            repository.updateDisplayName(value)
            syncRemoteProfile(mapOf("display_name" to value))
        }
    }

    fun updatePersonalData(lastName: String, email: String, birthDate: String) {
        viewModelScope.launch {
            repository.updateLastName(lastName)
            repository.updateEmail(email)
            repository.updateBirthDate(birthDate)
            syncRemoteProfile(mapOf(
                "last_name" to lastName,
                "email" to email,
                "birth_date" to birthDate
            ))
        }
    }

    private suspend fun syncRemoteProfile(data: Map<String, String>) {
        val prefs = repository.preferencesFlow.first()
        if (prefs.userId.isNotBlank()) {
            val result = sincronizacionRepository.updateRemoteProfile(prefs.userId, data)
            result.onFailure { e ->
                android.util.Log.e("ProfileVM", "Failed to sync remote profile: ${e.message}")
            }
        }
    }

    fun updateProfileImage(uri: String?) {
        viewModelScope.launch {
            repository.updateProfileImageUri(uri)
            syncRemoteProfile(mapOf("profile_image_url" to (uri ?: "")))
        }
    }

    fun updateCurrencyCode(value: String) {
        viewModelScope.launch {
            repository.updateCurrencyCode(value)
            syncRemoteProfile(mapOf("currency_code" to value))
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.setLoggedIn(false)
            repository.updateAccessToken("")
            repository.updateUserId("")
        }
    }
}
