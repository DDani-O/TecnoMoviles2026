package com.undef.fintrackmobile.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.fintrackmobile.data.network.NetworkModule
import com.undef.fintrackmobile.data.network.dto.AuthRequestDto
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
import com.undef.fintrackmobile.data.repository.SincronizacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

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
                NetworkModule.setAuthToken(response.accessToken)
                
                repository.updateEmail(email)
                repository.updateUserId(response.user.id)
                repository.updateAccessToken(response.accessToken)
                
                // Extraer metadata si existe
                response.user.userMetadata?.let { metadata ->
                    (metadata["display_name"] as? String)?.let { repository.updateDisplayName(it) }
                    (metadata["last_name"] as? String)?.let { repository.updateLastName(it) }
                    (metadata["birth_date"] as? String)?.let { repository.updateBirthDate(it) }
                }

                repository.setLoggedIn(true)
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                _uiState.value = AuthUiState.Error(parseError(e))
            }
        }
    }

    private fun parseError(e: Throwable): String {
        return if (e is HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                if (errorBody != null) {
                    Log.d("AuthViewModel", "Full error body: $errorBody")
                    val json = JSONObject(errorBody)
                    val desc = json.optString("error_description", "")
                    val msg = json.optString("msg", "")
                    val message = json.optString("message", "")
                    
                    val rawError = when {
                        desc.isNotBlank() -> desc
                        msg.isNotBlank() -> msg
                        message.isNotBlank() -> message
                        else -> ""
                    }

                    when {
                        rawError.contains("Email not confirmed", ignoreCase = true) -> 
                            "Por favor, confirma tu correo electrónico para poder entrar."
                        rawError.contains("Invalid login credentials", ignoreCase = true) -> 
                            "Correo o contraseña incorrectos."
                        rawError.contains("User already registered", ignoreCase = true) -> 
                            "Este correo ya está registrado."
                        rawError.contains("rate limit", ignoreCase = true) -> 
                            "Demasiados intentos. Por favor, espera un momento."
                        rawError.isNotBlank() -> rawError
                        else -> "Error: ${e.code()}"
                    }
                } else {
                    "Error: ${e.code()}"
                }
            } catch (_: Exception) {
                e.message() ?: "Network error"
            }
        } else {
            e.message ?: "Authentication failed"
        }
    }

    fun register(displayName: String, email: String, password: String, lastName: String, birthDate: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = sincronizacionRepository.signup(
                AuthRequestDto(
                    email = email,
                    password = password,
                    data = mapOf(
                        "display_name" to displayName,
                        "last_name" to lastName,
                        "birth_date" to birthDate
                    )
                )
            )
            result.onSuccess { response ->
                NetworkModule.setAuthToken(response.accessToken)
                
                repository.updateDisplayName(displayName)
                repository.updateLastName(lastName)
                repository.updateEmail(email)
                repository.updateBirthDate(birthDate)
                
                repository.updateUserId(response.user.id)
                repository.updateAccessToken(response.accessToken)
                repository.setLoggedIn(true)
                _uiState.value = AuthUiState.Success
            }.onFailure { e ->
                _uiState.value = AuthUiState.Error(parseError(e))
            }
        }
    }
}
