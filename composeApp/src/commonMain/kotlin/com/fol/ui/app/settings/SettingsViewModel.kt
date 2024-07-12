package com.fol.com.fol.ui.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fol.com.fol.db.AppProfile
import com.fol.com.fol.db.AppSettings
import com.fol.com.fol.model.DiGraph
import com.fol.model.repo.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SettingsUiState {
    data object Loading : SettingsUiState()
    data class Ready(
        val user: AppProfile,
        val darkTheme: Boolean,
        val systemTheme: Boolean,
    ) : SettingsUiState()

    data object Error : SettingsUiState()
    data object Deleted : SettingsUiState()
}

class SettingsViewModel(
    private val accountRepository: AccountRepository = DiGraph.accountRepository,
    private val appSettings: AppSettings = DiGraph.appSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _uiState.update {
                    SettingsUiState.Ready(
                        user = accountRepository.currentUser,
                        darkTheme = appSettings.isDarkTheme(),
                        systemTheme = appSettings.isSystemTheme(),
                    )
                }
            } catch (e: Exception) {
                _uiState.update { SettingsUiState.Error }
            }
        }
    }

    fun updateDarkTheme(darkTheme: Boolean) {
        appSettings.setDarkTheme(darkTheme)
        _uiState.update {
            SettingsUiState.Ready(
                user = accountRepository.currentUser,
                darkTheme = darkTheme,
                systemTheme = appSettings.isDarkTheme(),
            )
        }
    }

    fun updateSystemTheme(systemTheme: Boolean) {
        appSettings.setSystemTheme(systemTheme)
        _uiState.update {
            SettingsUiState.Ready(
                user = accountRepository.currentUser,
                darkTheme = appSettings.isDarkTheme(),
                systemTheme = systemTheme,
            )
        }
    }

    fun deleteAccount() {
        accountRepository.deleteAccount()
        _uiState.update { SettingsUiState.Deleted }
    }

}
