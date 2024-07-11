package com.fol.com.fol.ui.app.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fol.com.fol.db.AppProfile
import com.fol.com.fol.model.DiGraph
import com.fol.model.repo.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ProfileUiState{
    data object Loading : ProfileUiState()
    data class Ready(val user: AppProfile) : ProfileUiState()
    data object Error : ProfileUiState()
}

class ProfileViewModel(
    private val accountRepository: AccountRepository = DiGraph.accountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init{
        viewModelScope.launch {
            try {
                _uiState.update {
                    ProfileUiState.Ready(accountRepository.currentUser)
                }
            }catch(e: Exception){
                _uiState.update { ProfileUiState.Error }
            }
        }
    }

}
