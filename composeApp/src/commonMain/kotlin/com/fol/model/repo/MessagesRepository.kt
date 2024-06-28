package com.fol.com.fol.model.repo

import com.fol.com.fol.model.Contact
import com.fol.com.fol.model.Message
import com.fol.com.fol.model.ThreadPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock

class MessagesRepository(
    private val coroutineScope: CoroutineScope,
    private val contactsRepository : ContactsRepository
) {

    private val _threadPreviews = MutableStateFlow(emptyList<ThreadPreview>())
    val threadPreviews: StateFlow<List<ThreadPreview>> = _threadPreviews.asStateFlow()

    init{
        coroutineScope.launch {
            loadThreads()
        }
    }

    private suspend fun loadThreads() {
        _threadPreviews.update {
            emptyList()
        }

        contactsRepository.contacts.map { contacts ->
            _threadPreviews.update {
                contacts.map { loadMessagePreview(it) }
            }
        }.stateIn(coroutineScope)
    }

    private fun loadMessagePreview(contact: Contact) : ThreadPreview {
        return ThreadPreview(
            id = contact.name,
            contact = contact,
            lastMessage = Message("...", Clock.System.now() )
        )
    }

}
