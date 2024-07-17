package com.fol.com.fol.model.repo

import co.touchlab.kermit.Logger
import com.fol.com.fol.db.DbManager
import com.fol.com.fol.db.model.AppContact
import com.fol.com.fol.db.model.AppMessage
import com.fol.com.fol.db.model.AppProfile
import com.fol.com.fol.model.Message
import com.fol.com.fol.model.ThreadPreview
import com.fol.com.fol.network.ServerMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock

class MessagesRepository(
    private val coroutineScope: CoroutineScope,
    private val contactsRepository : ContactsRepository,
    private val dbManager: DbManager
) {

    private val _threadPreviews = MutableStateFlow(emptyList<ThreadPreview>())
    val threadPreviews: StateFlow<List<ThreadPreview>> = _threadPreviews.asStateFlow()

    init{
        coroutineScope.launch {
            loadThreads()
        }
    }

    private suspend fun loadThreads() {
        contactsRepository.allContacts.map { contacts ->
            _threadPreviews.update {
                contacts.map { contact ->
                    ThreadPreview(
                        id = contact.id,
                        contact = contact,
                        lastMessage = Message("...", Clock.System.now() , contact)
                    )
                }
            }
        }.stateIn(coroutineScope)
    }

    fun getMessagesFroThread(targetUser: AppContact, sender: AppProfile): Flow<List<AppMessage>> {
        return dbManager.getMessages(targetUser.publicKey, sender.publicKey)
    }

    fun addMessage(message: AppMessage) {
        Logger.i{ "sendMessage message: $message" }
        dbManager.addMessage(message)
    }

    fun addMessageFromServer(message: ServerMessage) {

    }

    fun gotDeliveryFromServer(deliveredId: List<Int>) {

    }

}
