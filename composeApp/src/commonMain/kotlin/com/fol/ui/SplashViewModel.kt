package com.fol.com.fol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fol.com.fol.model.DiGraph
import com.fol.model.repo.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SplashUiState{
    data object Loading : SplashUiState()
    data object Ready : SplashUiState()
    data object NoAccount : SplashUiState()
    data object Error : SplashUiState()
}

class SplashViewModel(
    private val accountRepository: AccountRepository = DiGraph.accountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init{
        viewModelScope.launch {
            try{
                val user = accountRepository.loadUser()
                if(user != null){
                    _uiState.update { SplashUiState.Ready }
                }else{
                    _uiState.update { SplashUiState.NoAccount }
                }
            }catch(e: Exception){
                _uiState.update { SplashUiState.Error }
            }
        }
    }

}
