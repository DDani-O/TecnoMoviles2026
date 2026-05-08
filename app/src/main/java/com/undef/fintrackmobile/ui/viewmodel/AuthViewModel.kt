package com.undef.fintrackmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
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

    fun register(displayName: String, email: String, lastName: String? = null, birthDate: String? = null) {
        viewModelScope.launch {
            if (displayName.isNotBlank()) {
                repository.updateDisplayName(displayName)
            }
            if (email.isNotBlank()) {
                repository.updateEmail(email)
            }
            lastName?.let {
                if (it.isNotBlank()) repository.updateLastName(it)
            }
            birthDate?.let {
                if (it.isNotBlank()) repository.updateBirthDate(it)
            }
            repository.setLoggedIn(value = true)
        }
    }
}
