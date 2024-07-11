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
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.fold
import kotlin.collections.map
import kotlin.collections.mapOf
import kotlin.collections.set

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

val gson = Gson()

fun Application.module() {

    // Initialize Database
    Database.connect("jdbc:sqlite:./data.db", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(Users, Messages)
    }

    // Track active WebSocket sessions
    val sessions = ConcurrentHashMap<Int, DefaultWebSocketServerSession>()

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
                var userId: Int? = null
                try {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val request = gson.fromJson(frame.readText(), WebSocketRequest::class.java)
                                when (request.type) {
                                    "connect" -> {
                                        userId = request.data["userId"]!!.toInt()
                                        sessions[userId] = this
                                    }
                                    "send" -> {
                                        handleSendMessage(request.data, sessions)
                                    }
                                    "receive" -> {
                                        userId?.let {
                                            val messages = handleReceiveMessages(it)
                                            outgoing.send(Frame.Text(gson.toJson(WebSocketResponse("messages", messages))))
                                        }
                                    }
                                    "confirm_delivery" -> {
                                        val messageIds = request.data["messageIds"]!!.split(",").map { it.toInt() }
                                        handleConfirmDelivery(messageIds)
                                    }
                                }
                            }
                            else -> Unit
                        }
                    }
                } finally {
                    userId?.let { sessions.remove(it) }
                }
            }
        }
}

fun hashPublicKey(publicKey: String): Int {
    val bytes = MessageDigest.getInstance("SHA-256").digest(publicKey.toByteArray())
    return bytes.fold(0) { acc, byte -> (acc * 31) + byte.toInt() }
}

suspend fun DefaultWebSocketServerSession.handleSendMessage(data: Map<String, String>, sessions: ConcurrentHashMap<Int, DefaultWebSocketServerSession>) {
    val publicKey = data["publicKey"]!!
    val recipientId = data["recipientId"]!!.toInt()
    val encryptedMessage = data["encryptedMessage"]!!
    val timestamp = data["timestamp"]!!.toLong()
    val senderId = hashPublicKey(publicKey)
    val messageId = transaction {
        Users.insertIgnore {
            it[Users.userId] = senderId
            it[Users.publicKey] = publicKey
        }
        Messages.insertAndGetId {
            it[Messages.senderId] = senderId
            it[Messages.recipientId] = recipientId
            it[Messages.encryptedMessage] = encryptedMessage
            it[Messages.timestamp] = timestamp
        }
    }

    // Send the message in real time if the recipient is connected
    sessions[recipientId]?.let {
        val message = MessageResponse(messageId.value, senderId, recipientId, encryptedMessage, timestamp)
        it.outgoing.send(Frame.Text(gson.toJson(WebSocketResponse("new_message", message))))
    }

    outgoing.send(Frame.Text(gson.toJson(WebSocketResponse("status", mapOf("status" to "success")))))
}

fun handleReceiveMessages(userId: Int): List<MessageResponse> {
    return transaction {
        Messages.select { Messages.recipientId eq userId }.map {
            MessageResponse(
                it[Messages.id].value,
                it[Messages.senderId],
                it[Messages.recipientId],
                it[Messages.encryptedMessage],
                it[Messages.timestamp]
            )
        }
    }
}

fun handleConfirmDelivery(messageIds: List<Int>) {
    transaction {
        Messages.deleteWhere { Messages.id inList messageIds }
    }
}

data class WebSocketRequest(val type: String, val data: Map<String, String>)
data class WebSocketResponse(val type: String, val data: Any)
data class SendMessageRequest(val publicKey: String, val recipientId: Int, val encryptedMessage: String, val timestamp: Long)
data class MessageResponse(val messageId: Int, val senderId: Int, val recipientId: Int, val encryptedMessage: String, val timestamp: Long)
data class DeliveryConfirmation(val messageIds: List<Int>)

// Database tables
object Users : IntIdTable() {
    val userId = integer("user_id").uniqueIndex()
    val publicKey = text("public_key")
}

object Messages : IntIdTable() {
    val senderId = integer("sender_id")
    val recipientId = integer("recipient_id")
    val encryptedMessage = text("encrypted_message")
    val timestamp = long("timestamp")
}

