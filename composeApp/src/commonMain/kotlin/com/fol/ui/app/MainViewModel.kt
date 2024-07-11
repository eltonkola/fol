package com.fol.com.fol.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fol.com.fol.db.AppProfile
import com.fol.com.fol.model.DiGraph
import com.fol.com.fol.model.ThreadPreview
import com.fol.com.fol.model.repo.MessagesRepository
import com.fol.model.repo.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val user: AppProfile,
    val threads: List<ThreadPreview> = emptyList(),
)

class MainViewModel(
    private val accountRepository: AccountRepository = DiGraph.accountRepository,
    private val messagesRepository: MessagesRepository = DiGraph.messagesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState(accountRepository.currentUser))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init{
        viewModelScope.launch {
            messagesRepository.threadPreviews.map { data ->
                _uiState.update {
                    it.copy(threads = data)
                }
            }.stateIn(viewModelScope)
        }
    }

}
