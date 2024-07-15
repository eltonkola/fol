package com.fol.com.fol.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fol.com.fol.db.AppSettings
import com.fol.com.fol.model.DiGraph
import com.fol.model.repo.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LandingUiState(
    val acceptedTerms: Boolean,
    val showTerms: Boolean = false
)

class LandingScreenViewModel(
    private val appSettings: AppSettings = DiGraph.appSettings
) : ViewModel() {
    private val _uiState = MutableStateFlow(LandingUiState(appSettings.acceptedTerms()))
    val uiState: StateFlow<LandingUiState> = _uiState.asStateFlow()

    fun showTerms(){
        _uiState.update { it.copy(showTerms = true) }
    }

    fun accept(){
        appSettings.setAcceptTerms(true)
        _uiState.update { it.copy(showTerms = false, acceptedTerms = true) }
    }

}
