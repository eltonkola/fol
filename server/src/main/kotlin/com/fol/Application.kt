package com.fol

import SERVER_PORT
import com.google.gson.Gson
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.first
import kotlin.collections.map
import kotlin.collections.set

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

val gson = Gson()

val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()

fun Application.module() {
    Database.connect("jdbc:sqlite:./data.db", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(Messages)
    }
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }
        install(WebSockets) {
//            pingPeriod = Duration.ofMinutes(1)
//            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        routing {
            webSocket ("/ws") {
                var user: String? = null
                try {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val request = gson.fromJson(frame.readText(), WebSocketRequest::class.java)
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
                                            println("Receive : messages = $messages")
                                            outgoing.send(Frame.Text(gson.toJson(WebSocketResponse(RespType.Messages.value, messages))))
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
                                        outgoing.send(Frame.Text(gson.toJson(WebSocketResponse(RespType.ConfirmationDelivered.value, MessageDeliveredConfirmation(delivered)))))
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
    sessions: ConcurrentHashMap<String, DefaultWebSocketServerSession>) {

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
        it.outgoing.send(Frame.Text(gson.toJson(WebSocketResponse(RespType.NewMessage.value, messageRealTime))))
    }
    //send the conformation to the sender himself, with the remote id
    val confirmation = MessageSentConfirmation(localId = localId, remoteId = messageId.value)
    outgoing.send(Frame.Text(gson.toJson(WebSocketResponse(RespType.ConfirmationSent.value, confirmation))))
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
                    gson.toJson(
                        WebSocketResponse(
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

data class WebSocketRequest(val type: String, val data: Map<String, String>)
data class WebSocketResponse(val type: String, val data: Any)

//message response sent to the client
data class MessageResponse(val remoteId: Int, val senderKey: String, val receiverKey: String, val message: String, val timestamp: Long)
//when sending a message the client gets a message with the confirmation the message as been saved
data class MessageSentConfirmation(val localId: String, val remoteId: Int)
data class MessageDeliveredConfirmation(val ids: List<Int>)


// Database tables
object Messages : IntIdTable() {
    val senderKey = text("senderKey")
    val receiverKey = text("receiverKey")
    val message = text("message")
    val timestamp = long("timestamp")
}

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
