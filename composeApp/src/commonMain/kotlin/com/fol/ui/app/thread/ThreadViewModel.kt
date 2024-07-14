package com.fol.com.fol.ui.app.thread

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fol.com.fol.db.AppContact
import com.fol.com.fol.db.AppProfile
import com.fol.com.fol.model.DiGraph
import com.fol.com.fol.model.Message
import com.fol.com.fol.model.repo.ContactsRepository
import com.fol.com.fol.model.repo.MessagesRepository
import com.fol.model.repo.AccountRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ThreadUiState(
    val user: AppProfile,
    val contact: AppContact?= null,
    val contactStatus: ContactStatus = ContactStatus.Idle,
    val messages: List<Message> = emptyList(),
)

sealed class ContactStatus{
    data object Idle : ContactStatus()
    data object ConformDelete : ContactStatus()
    data object Deleting : ContactStatus()
    data object Deleted : ContactStatus()
    data object ErrorDeleting : ContactStatus()
    data object ErrorLoading : ContactStatus()
    data object Details : ContactStatus()
}

class ThreadViewModel(
    private val userId: String,
    private val accountRepository: AccountRepository = DiGraph.accountRepository,
    private val contactsRepository: ContactsRepository = DiGraph.contactsRepository,
    private val messagesRepository: MessagesRepository = DiGraph.messagesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ThreadUiState(accountRepository.currentUser))
    val uiState: StateFlow<ThreadUiState> = _uiState.asStateFlow()

    fun onDeleteContact() {
        _uiState.update { it.copy(contactStatus = ContactStatus.ConformDelete) }
    }

    fun confirmDelete() {
        viewModelScope.launch {
            _uiState.update { it.copy(contactStatus = ContactStatus.Deleting) }
            try{
                contactsRepository.deleteContact(_uiState.value.contact!!)
                delay(2000)
                _uiState.update { it.copy(contactStatus = ContactStatus.Deleted) }
            }catch (e: Exception){
                _uiState.update { it.copy(contactStatus = ContactStatus.ErrorDeleting) }
            }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(contactStatus = ContactStatus.Idle) }
    }

    fun showContact() {
        if(uiState.value.contact != null) {
            _uiState.update { it.copy(contactStatus = ContactStatus.Details) }
        }
    }

    init{
        viewModelScope.launch {
            val targetUser = contactsRepository.getContactById(userId)
            if(targetUser !=null){

                _uiState.update { it.copy(contact = targetUser) }

                messagesRepository.getThreads(targetUser)
                    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
                    .collect { messages ->
                        _uiState.update { it.copy(messages = messages) }
                    }

            }else{
                _uiState.update { it.copy(contactStatus = ContactStatus.ErrorLoading) }
            }

        }
    }






    //fake chat ui

    val conversation: StateFlow<List<ChatUiModel.Message>>
        get() = _conversation
    private val _conversation = MutableStateFlow(
        listOf(ChatUiModel.Message.initConv)
    )

    private val questions = mutableListOf(
        "What about yesterday?",
        "Can you tell me what inside your head?",
        "Lately, I've been wondering if I can really do anything, do you?",
        "You know fear is often just an illusion, have you ever experienced it?",
        "If you were me, what would you do?"
    )

    fun sendChat(msg: String) {

        val myChat = ChatUiModel.Message(msg, ChatUiModel.Author.me)
        viewModelScope.launch {
            _conversation.emit(_conversation.value + myChat)

            delay(1000)
            _conversation.emit(_conversation.value + getRandomQuestion())
        }
    }

    private fun getRandomQuestion(): ChatUiModel.Message {
        val question = if (questions.isEmpty()) {
            "no further questions, please leave me alone"
        } else {
            questions.random()
        }

        if (questions.isNotEmpty()) questions.remove(question)

        return ChatUiModel.Message(
            text = question,
            author = ChatUiModel.Author.bot
        )
    }

}
