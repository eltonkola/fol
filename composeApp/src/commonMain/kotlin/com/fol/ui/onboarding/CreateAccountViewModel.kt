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

sealed class CreateAccountUiState {
    data object Idle : CreateAccountUiState()
    data object Creating : CreateAccountUiState()
    data object Error : CreateAccountUiState()
    data object Created : CreateAccountUiState()
}

class CreateAccountViewModel(
    private val accountRepository: AccountRepository = DiGraph.accountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<CreateAccountUiState>(CreateAccountUiState.Idle)
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    fun createAccount(){
        _uiState.update { CreateAccountUiState.Creating }
        viewModelScope.launch {
            try{
                val user = accountRepository.createUser("elton", "public", "private")
                if(user != null){
                    _uiState.update { CreateAccountUiState.Created }
                }else{
                    _uiState.update { CreateAccountUiState.Error }
                }
            }catch(e: Exception){
                _uiState.update { CreateAccountUiState.Error }
            }
        }
    }

    fun resetForm(){
        _uiState.update { CreateAccountUiState.Idle }
    }

}
