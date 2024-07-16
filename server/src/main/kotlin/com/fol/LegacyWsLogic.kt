package com.fol

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()

fun Application.legacyWs() {
    routing {
        webSocket ("/ws") {
            var user: String? = null
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {

                            val text = frame.readText()
                            val request = Json.decodeFromString<WebSocketRequest>(text)

                            when (MsgType.fromValue(request.type)) {
                                MsgType.Connect -> { //handshake, cache this session
                                    user = request.data[SENDER_KEY]!!
                                    sessions[user] = this
                                }
                                MsgType.Send -> {
                                    handleSendMessage(request.data, sessions)
                                }
                                MsgType.Receive-> { //get all messages you missed when offline
                                    user?.let {
                                        println("Receive : input = $it")
                                        val messages = handleReceiveMessages(it)
                                        val response = ReceiveMessagesResponse(RespType.Messages.value, messages)
                                        println("Receive : messages = $messages")
                                        outgoing.send(Frame.Text(Json.encodeToString(response)))
                                    }
                                }
                                MsgType.ConfirmReceived -> { //when you get a message, tell the server to delete it
                                    val messageIds = request.data["messageIds"]!!.split(",").map { it.toInt() }
                                    println("ConfirmReceived : messageIds = $messageIds")
                                    handleConfirmDelivery(messageIds, sessions)
                                }
                                MsgType.CheckDelivery -> { // if these ids are not in the db, they have been delivered, or deleted!
                                    val messageIds = request.data["messageIds"]!!.split(",").map { it.toInt() }
                                    println("CheckDelivery : messageIds = $messageIds")
                                    val delivered = getDeliveredMessagesIds(messageIds)
                                    println("CheckDelivery : delivered = $delivered")
                                    outgoing.send(Frame.Text(Json.encodeToString(WebSocketResponseMessageDeliveredConfirmation(RespType.ConfirmationDelivered.value, MessageDeliveredConfirmation(delivered)))))
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            } finally {
                user?.let { sessions.remove(it) }
            }
        }
    }
}


suspend fun DefaultWebSocketServerSession.handleSendMessage(
    data: Map<String, String>,
    sessions: ConcurrentHashMap<String, DefaultWebSocketServerSession>
) {

    val senderKey = data["senderKey"]!!
    val receiverKey = data["receiverKey"]!!
    val message = data["message"]!!
    val timestamp = data["timestamp"]!!.toLong()
    val localId = data["localId"]!!

    val messageId = transaction {
        Messages.insertAndGetId {
            it[Messages.senderKey] = senderKey
            it[Messages.receiverKey] = receiverKey
            it[Messages.message] = message
            it[Messages.timestamp] = timestamp
        }
    }

    // Send the message in real time if the recipient is connected
    sessions[receiverKey]?.let {
        val messageRealTime = MessageResponse(messageId.value, senderKey, receiverKey, message, timestamp)
        it.outgoing.send(Frame.Text(Json.encodeToString(WebSocketResponseMessageResponse(RespType.NewMessage.value, messageRealTime))))
    }
    //send the conformation to the sender himself, with the remote id
    val confirmation = MessageSentConfirmation(localId = localId, remoteId = messageId.value)
    outgoing.send(Frame.Text(Json.encodeToString(WebSocketResponseMessageSentConfirmation(RespType.ConfirmationSent.value, confirmation))))
}


fun handleReceiveMessages(receiverKey: String): List<MessageResponse> {
    return transaction {
        Messages.select { Messages.receiverKey eq receiverKey }.map {
            MessageResponse(
                it[Messages.id].value,
                it[Messages.senderKey],
                it[Messages.receiverKey],
                it[Messages.message],
                it[Messages.timestamp]
            )
        }
    }
}

suspend fun DefaultWebSocketServerSession.handleConfirmDelivery(
    messageIds: List<Int>,
    sessions: ConcurrentHashMap<String, DefaultWebSocketServerSession>
) {
    val messages = transaction {
        Messages.select { Messages.id inList messageIds }.map { Pair(it[Messages.senderKey], it[Messages.id].value) }
    }
    println("messages before delete: $messages")
    transaction {
        Messages.deleteWhere { Messages.id inList messageIds }
    }

    //for every message we delete, we send the original sender a message, in case they are connected!
    messages.forEach { message ->
        val receiverKey = message.first
        val id = message.second
        sessions[receiverKey]?.let {
            val deliveredMessage = MessageDeliveredConfirmation(listOf(id))
            outgoing.send(
                Frame.Text(
                    Json.encodeToString(
                        WebSocketResponseMessageDeliveredConfirmation(
                            RespType.ConfirmationDelivered.value,
                            deliveredMessage
                        )
                    )
                )
            )
        }
    }

}


fun getDeliveredMessagesIds(messageIds: List<Int>) : List<Int> {
    return transaction {
        val notDelivered = Messages.select{ Messages.id inList messageIds }.map { it[Messages.id].value }
        messageIds.filter { !notDelivered.contains(it) }
    }
}



@Serializable
data class ReceiveMessagesResponse(val type: String, val data: List<MessageResponse>)

@Serializable
data class WebSocketRequest(val type: String, val data: Map<String, String>)
@Serializable
data class WebSocketResponseMessageResponse(val type: String, val data: MessageResponse)
@Serializable
data class WebSocketResponseMessageSentConfirmation(val type: String, val data: MessageSentConfirmation)
@Serializable
data class WebSocketResponseMessageDeliveredConfirmation(val type: String, val data: MessageDeliveredConfirmation)


@Serializable
data class MessageResponse(val remoteId: Int, val senderKey: String, val receiverKey: String, val message: String, val timestamp: Long)
@Serializable
data class MessageSentConfirmation(val localId: String, val remoteId: Int)
@Serializable
data class MessageDeliveredConfirmation(val ids: List<Int>)




const val SENDER_KEY = "senderKey"

enum class MsgType(val value: String){
    Connect("connect"),
    Send("send"),
    Receive("receive"),
    CheckDelivery("check_delivery"),
    ConfirmReceived("confirm_received");

    companion object {
        fun fromValue(value: String): MsgType {
            return entries.first{ it.value == value }
        }
    }
}

enum class RespType(val value: String){
    ConfirmationSent("confirmation_sent"),
    NewMessage("new_message"),
    Messages("messages"),
    ConfirmationDelivered("confirmation_delivered");
}

