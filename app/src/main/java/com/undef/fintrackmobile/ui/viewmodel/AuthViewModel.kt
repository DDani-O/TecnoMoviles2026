package com.undef.fintrackmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.fintrackmobile.data.network.dto.AuthRequestDto
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
import com.undef.fintrackmobile.data.repository.SincronizacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repository: UserPreferencesRepository,
    private val sincronizacionRepository: SincronizacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = sincronizacionRepository.login(AuthRequestDto(email, password))
            result.onSuccess { response ->
                repository.updateEmail(email)
                repository.updateUserId(response.user.id)
                repository.updateAccessToken(response.accessToken)
                repository.setLoggedIn(true)
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                _uiState.value = AuthUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register(displayName: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = sincronizacionRepository.signup(
                AuthRequestDto(
                    email = email,
                    password = password,
                    data = mapOf("display_name" to displayName)
                )
            )
            result.onSuccess { response ->
                repository.updateDisplayName(displayName)
                repository.updateEmail(email)
                repository.updateUserId(response.user.id)
                repository.updateAccessToken(response.accessToken)
                repository.setLoggedIn(true)
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                _uiState.value = AuthUiState.Error(e.message ?: "Registration failed")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.setLoggedIn(false)
            repository.updateAccessToken("")
            repository.updateUserId("")
            _uiState.value = AuthUiState.Idle
        }
    }
}
