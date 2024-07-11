package com.fol.com.fol.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fol.com.fol.db.AppProfile
import com.fol.com.fol.model.DiGraph
import com.fol.com.fol.model.ThreadPreview
import com.fol.com.fol.model.repo.MessagesRepository
import com.fol.model.repo.AccountRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ThreadUiState(
    val user: AppProfile,
    val threads: List<ThreadPreview> = emptyList(),
)

class ThreadViewModel(
    private val accountRepository: AccountRepository = DiGraph.accountRepository,
    private val messagesRepository: MessagesRepository = DiGraph.messagesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState(accountRepository.currentUser))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()


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
