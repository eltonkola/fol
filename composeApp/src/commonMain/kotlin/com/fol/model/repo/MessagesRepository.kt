package com.fol.com.fol.model.repo

import co.touchlab.kermit.Logger
import com.fol.com.fol.db.DbManager
import com.fol.com.fol.db.model.AppContact
import com.fol.com.fol.db.model.AppMessage
import com.fol.com.fol.db.model.AppProfile
import com.fol.com.fol.model.DiGraph
import com.fol.com.fol.model.Message
import com.fol.com.fol.model.ThreadPreview
import com.fol.com.fol.network.DeliveryCheckRequest
import com.fol.com.fol.network.DeliveryCheckResponse
import com.fol.com.fol.network.GetMessageResponse
import com.fol.com.fol.network.MessageReceivedRequest
import com.fol.com.fol.network.MessageReceivedResponse
import com.fol.com.fol.network.NetworkManager
import com.fol.com.fol.network.NetworkOperations
import com.fol.com.fol.network.SendMessageRequest
import com.fol.com.fol.network.SendMessageResponse
import com.fol.com.fol.network.ServerMessage
import com.fol.com.fol.network.toServerMessage
import com.fol.model.repo.AccountRepository
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

interface MessageEventReceiver{
    fun addMessageFromServer(message: ServerMessage)
    fun gotDeliveryFromServer(deliveredId: List<Int>)
}

class MessagesRepository(
    private val coroutineScope: CoroutineScope,
    private val contactsRepository : ContactsRepository,
    private val dbManager: DbManager,
    private val networkManager: NetworkOperations,
    private val accountRepository: AccountRepository
) {

//    private val networkManager : NetworkOperations = FakeNetwrok()

    private val _threadPreviews = MutableStateFlow(emptyList<ThreadPreview>())
    val threadPreviews: StateFlow<List<ThreadPreview>> = _threadPreviews.asStateFlow()

    init{
        coroutineScope.launch {
            loadThreads()
        }
        coroutineScope.launch {
            syncMessages()
        }
        coroutineScope.launch {
            sendUnsent()
        }
    }

    private suspend fun loadThreads() {
        contactsRepository.allContacts.map { contacts ->
            _threadPreviews.update {
                contacts.map { contact ->
                    ThreadPreview(
                        id = contact.id,
                        contact = contact,
                        lastMessage = Message("...", Clock.System.now() , false, false,contact)
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

    //the server sent us a message, save it
    fun addMessageFromServer(message: ServerMessage) {
        Logger.i { "addMessageFromServer message: $message" }
        saveMessage(message)
    }

    //the server just live confirmed a message was delivered
    fun gotDeliveryFromServer(deliveredId: List<Int>) {
        Logger.i { "gotDeliveryFromServer : $deliveredId" }
        deliveredId.forEach { messageConfirmedAsDelivered(it) }
    }

    private suspend fun syncMessages(){
        Logger.i { ">>>> start syncMessages <<<<<" }
        //1. - load all unread messages from server
        Logger.i { "[1] Get new messages" }
        val newMessages = networkManager.getMessages()
        Logger.i { "new messages from server: ${newMessages.messages.size}" }
        val savedMessages = newMessages.messages.filter { message -> saveMessage(message) }.map { it.remoteId }
        Logger.i { "saved messages: ${savedMessages.size}" }
        val response = networkManager.received(MessageReceivedRequest(messageIds = savedMessages))
        Logger.i { "successful sent notice: ${response.success}" } //TODO - if this fails, we will have zombie messages on the server.

        //2. check for all send messages if are delivered
        Logger.i { "[2] Check if my messages are delivered" }
        val sentMessages = dbManager.getAllSentMessages().map { it.serverID }
        Logger.i { "sent messages, we don't know are delivered: ${sentMessages.size}" }
        val deliveredMessages = networkManager.check(DeliveryCheckRequest(messageIds = sentMessages))
        Logger.i { "delivery update: ${deliveredMessages.deliveredId.size}" }
        deliveredMessages.deliveredId.forEach { messageConfirmedAsDelivered(it) }

        Logger.i { ">>>> end syncMessages <<<<<" }
    }

    private suspend fun sendUnsent(){
        //3. send unsent messages should happen automatically
        Logger.i { "[3] Listen to all new saved and unset messages, and sent them" }
        dbManager.unsentMessages(accountRepository.currentUser.publicKey)
            .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())
            .collect { messages ->
                Logger.i { "unsent messages: ${messages.size} -> $messages" }
                val toSent = messages.map { it.toServerMessage() }
                networkManager.sendMessages(toSent)
            }
    }

    //the server told us this message as been delivered, update it
    private fun messageConfirmedAsDelivered(messageId: Int) {
        dbManager.messageConfirmedAsDelivered(messageId)
    }

    //got a new message, save it, if successful return the true
    private fun saveMessage(serverMessage: ServerMessage) : Boolean {
        return try{
            val localMessage = AppMessage()
            localMessage.apply {
                message = serverMessage.message
                senderKey = serverMessage.senderKey
                receiverKey = serverMessage.receiverKey
                sent =  true
                received = true
                timeSent = RealmInstant.from(serverMessage.timestamp, 0)
                serverID  = serverMessage.remoteId
            }
            addMessage(localMessage)
            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

}
