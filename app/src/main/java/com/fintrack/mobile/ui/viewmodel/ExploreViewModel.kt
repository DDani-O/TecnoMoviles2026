package com.fintrack.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintrack.mobile.data.repository.ExploreRepository
import com.fintrack.mobile.data.repository.RateResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ExploreUiState {
    data object Loading : ExploreUiState()
    data class Ready(val rate: Double, val isFallback: Boolean) : ExploreUiState()
}

class ExploreViewModel(
    private val repository: ExploreRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    fun loadRate(base: String, target: String) {
        viewModelScope.launch {
            when (val result = repository.getRate(base, target)) {
                is RateResult.Success -> _state.value = ExploreUiState.Ready(rate = result.rate, isFallback = false)
                is RateResult.Fallback -> _state.value = ExploreUiState.Ready(rate = result.rate, isFallback = true)
            }
        }
    }
}
