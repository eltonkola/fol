package com.fol.com.fol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.fol.com.fol.db.DbManager
import com.fol.com.fol.model.DiGraph
import com.fol.model.repo.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SplashOpState{
    data object Loading : SplashOpState()
    data object Ready : SplashOpState()
    data object NoAccount : SplashOpState()
    data object Error : SplashOpState()
    data object Pin : SplashOpState()
    data object PinError : SplashOpState()
}

data class SplashUiState(
    val state: SplashOpState = SplashOpState.Loading,
    val pin: String = ""
)

class SplashViewModel(
    private val accountRepository: AccountRepository = DiGraph.accountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init{
        viewModelScope.launch {
            try{
                val accountExists = accountRepository.accountExists()
                if(accountExists){
                    _uiState.update { it.copy(state = SplashOpState.Pin) }
                }else{
                    _uiState.update {it.copy(state = SplashOpState.NoAccount) }
                }
            }catch(e: Exception){
                _uiState.update { it.copy(state = SplashOpState.Error) }
            }
        }
    }

    fun updatePin(pin: String){
        if(pin.length<= 6){
            _uiState.update { it.copy(pin = pin) }
        }
    }

    fun authenticateUser(){
        try{
            _uiState.update { it.copy(state = SplashOpState.Loading) }
            val user = accountRepository.loadUser(_uiState.value.pin)
            Logger.i{ "User: $user" }
            if(user != null){
                _uiState.update { it.copy(state = SplashOpState.Ready) }
            }else{
                _uiState.update { it.copy(state = SplashOpState.NoAccount) }
            }
        }catch(e: Exception){
            Logger.e(e){ "error: ${e.message}" }
            _uiState.update { it.copy(state = SplashOpState.PinError) }
        }
    }

    fun retryPin() {
        _uiState.update { it.copy(state = SplashOpState.Pin, pin = "") }
    }

    fun nuke() {
        viewModelScope.launch {
            accountRepository.deleteAccount()
        }
    }

}
