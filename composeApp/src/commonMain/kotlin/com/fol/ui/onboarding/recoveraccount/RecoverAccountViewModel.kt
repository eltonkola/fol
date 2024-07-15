package com.fol.com.fol.ui.onboarding.recoveraccount

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

sealed class RecoveryState {
    data object Idle : RecoveryState()
    data object Recovering : RecoveryState()
    data object Error : RecoveryState()
    data object Recovered : RecoveryState()
}

data class RecoverAccountUiState(
    val state: RecoveryState = RecoveryState.Idle,
    val privateKey: String="",
    val publicKey: String="",
    val pin: String="",
    val invalidMessage: String? = null,
)



class RecoverAccountViewModel(
    private val accountRepository: AccountRepository = DiGraph.accountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecoverAccountUiState())
    val uiState: StateFlow<RecoverAccountUiState> = _uiState.asStateFlow()

    fun recoverAccount(){

        _uiState.update { it.copy(state = RecoveryState.Recovering)  }
        viewModelScope.launch {
            try{
                val user = accountRepository.recoverUser(
                    publicKey = _uiState.value.publicKey,
                    privateKey = _uiState.value.privateKey,
                    pin = _uiState.value.pin
                )
                if(user != null){
                    _uiState.update { it.copy(state = RecoveryState.Recovered) }
                }else{
                    _uiState.update { it.copy(state = RecoveryState.Error) }
                }
            }catch(e: Exception){
                e.printStackTrace()
                _uiState.update {it.copy(state = RecoveryState.Error) }
            }
        }
    }

    fun resetForm(){
        _uiState.update { it.copy(state = RecoveryState.Idle) }
    }

    fun updatePin(pin: String) {
        if(pin.length > 6){
            return
        }
        _uiState.update { it.copy(pin = pin) }
    }

    fun updatePrivateKey(privateKey: String) {
        viewModelScope.launch {
            if (CryptoManager.validatePrivateKey(privateKey)) {
                _uiState.update { it.copy(privateKey = privateKey, invalidMessage = null) }
            }
            _uiState.update { it.copy(invalidMessage = "Invalid Private Key!") }
        }
    }

    fun updatePublicKey(publicKey: String) {
        viewModelScope.launch {
            if (CryptoManager.validatePublicKey(publicKey)) {
                _uiState.update { it.copy(publicKey = publicKey, invalidMessage = null) }
            }
            _uiState.update { it.copy(invalidMessage = "Invalid Public Key!") }
        }
    }


}
