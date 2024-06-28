package com.fol.com.fol.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fol.com.fol.model.DiGraph
import com.fol.model.repo.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class RecoverAccountUiState {
    data object Idle : RecoverAccountUiState()
    data object Creating : RecoverAccountUiState()
    data object Error : RecoverAccountUiState()
    data object Recovered : RecoverAccountUiState()
}

class RecoverAccountViewModel(
    private val accountRepository: AccountRepository = DiGraph.accountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecoverAccountUiState>(RecoverAccountUiState.Idle)
    val uiState: StateFlow<RecoverAccountUiState> = _uiState.asStateFlow()

    fun recoverAccount(){
        _uiState.update { RecoverAccountUiState.Creating }
        viewModelScope.launch {
            try{
                val user = accountRepository.recoverUser("elton", "public", "private")
                if(user != null){
                    _uiState.update { RecoverAccountUiState.Recovered }
                }else{
                    _uiState.update { RecoverAccountUiState.Error }
                }
            }catch(e: Exception){
                _uiState.update { RecoverAccountUiState.Error }
            }
        }
    }

    fun resetForm(){
        _uiState.update { RecoverAccountUiState.Idle }
    }

}
