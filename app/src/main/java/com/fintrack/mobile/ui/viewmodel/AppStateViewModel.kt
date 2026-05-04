package com.fintrack.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.mobile.data.preferences.UserPreferences
import com.fintrack.mobile.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AppStateViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences.DEFAULT)
}
