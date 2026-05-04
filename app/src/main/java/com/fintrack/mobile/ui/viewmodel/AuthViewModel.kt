package com.fintrack.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.mobile.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: UserPreferencesRepository,
) : ViewModel() {
    fun login(displayName: String, email: String) {
        viewModelScope.launch {
            if (displayName.isNotBlank()) {
                repository.updateDisplayName(displayName)
            }
            if (email.isNotBlank()) {
                repository.updateEmail(email)
            }
            repository.setLoggedIn(value = true)
        }
    }

    fun register(displayName: String, email: String) {
        viewModelScope.launch {
            if (displayName.isNotBlank()) {
                repository.updateDisplayName(displayName)
            }
            if (email.isNotBlank()) {
                repository.updateEmail(email)
            }
            repository.setLoggedIn(value = true)
        }
    }
}
