package com.fol.com.fol.ui.app.addcontact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.fol.com.fol.crypto.CryptoManager
import com.fol.com.fol.model.DiGraph
import com.fol.com.fol.model.repo.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AddContactUiState{
    data class Ready(
        val publicKey: String = "",
        val name: String = "",
        val error: String? = null
    ) : AddContactUiState()
    data object Error : AddContactUiState()
    data object Loading : AddContactUiState()
}

class AddContactViewModel(
    private val contactsRepository: ContactsRepository = DiGraph.contactsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AddContactUiState>(AddContactUiState.Ready())
    val uiState: StateFlow<AddContactUiState> = _uiState.asStateFlow()

    fun updatePublicKey(publicKey: String) {
        viewModelScope.launch {
            if (CryptoManager.validatePublicKey(publicKey)) {
                _uiState.update {
                    val data = it as AddContactUiState.Ready
                    data.copy(publicKey = publicKey, error = null)
                }
            } else{
                _uiState.update {
                    val data = it as AddContactUiState.Ready
                    data.copy(publicKey = "", error = "Invalid Public Key")
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update {
            val data = it as AddContactUiState.Ready
            data.copy(name = name )
        }
    }

    fun addContact(onAdded: () -> Unit) {
        val data = _uiState.value as AddContactUiState.Ready

        viewModelScope.launch {
            _uiState.update { AddContactUiState.Loading }
            try {
                val added = contactsRepository.addContact(data.name, data.publicKey)
                if(added){
                    _uiState.update {
                        AddContactUiState.Ready("", "")
                    }
                    onAdded()
                }else{
                    _uiState.update { AddContactUiState.Error }
                }
            }catch(e: Exception){
                Logger.e("AddContactViewModel", e, "Error adding contact")
                _uiState.update { AddContactUiState.Error }
            }
        }
    }

}
