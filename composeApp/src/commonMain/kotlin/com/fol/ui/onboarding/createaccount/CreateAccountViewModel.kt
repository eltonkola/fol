package com.fol.com.fol.ui.onboarding.createaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fol.com.fol.crypto.CryptoManager
import com.fol.com.fol.model.DiGraph
import com.fol.model.repo.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class CreationState {
    data object Idle : CreationState()
    data object Creating : CreationState()
    data object Error : CreationState()
    data object Created : CreationState()
}

data class CreateAccountUiState(
    val state: CreationState = CreationState.Idle,
    val privateKey: String="",
    val publicKey: String="",
    val pin: String="",
)

class CreateAccountViewModel(
    private val accountRepository: AccountRepository = DiGraph.accountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateAccountUiState())
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    init{
        generateKey()
    }

    fun generateKey(){
        viewModelScope.launch {
            val keys = CryptoManager.generateKeyPair()
            _uiState.update { it.copy(publicKey = keys.rsaPublicKey, privateKey = keys.rsaPrivateKey) }
        }
    }

    fun createAccount(){
        _uiState.update { it.copy(state = CreationState.Creating)  }
        viewModelScope.launch {
            try{
                val user = accountRepository.createUser(
                    publicKey = _uiState.value.publicKey,
                    privateKey = _uiState.value.privateKey,
                    pin = _uiState.value.pin
                )
                if(user != null){
                    _uiState.update { it.copy(state = CreationState.Created) }
                }else{
                    _uiState.update { it.copy(state = CreationState.Error) }
                }
            }catch(e: Exception){
                e.printStackTrace()
                _uiState.update {it.copy(state = CreationState.Error) }
            }
        }
    }

    fun resetForm(){
        _uiState.update { it.copy(state = CreationState.Idle) }
    }

    fun updatePin(pin: String) {
        if(pin.length > 6){
            return
        }
        _uiState.update { it.copy(pin = pin) }
    }

}
