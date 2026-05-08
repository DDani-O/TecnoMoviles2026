package com.undef.fintrackmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.fintrackmobile.data.preferences.UserPreferences
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences.DEFAULT)

    fun updateDisplayName(value: String) {
        viewModelScope.launch {
            repository.updateDisplayName(value)
        }
    }

    fun updatePersonalData(lastName: String, email: String, birthDate: String) {
        viewModelScope.launch {
            repository.updateLastName(lastName)
            repository.updateEmail(email)
            repository.updateBirthDate(birthDate)
        }
    }

    fun updateProfileImage(uri: String?) {
        viewModelScope.launch {
            repository.updateProfileImageUri(uri)
        }
    }

    fun updateCurrencyCode(value: String) {
        viewModelScope.launch {
            repository.updateCurrencyCode(value)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.setLoggedIn(false)
        }
    }
}
