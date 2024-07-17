package com.fol

import com.fol.Messages.message
import com.fol.Messages.receiverKey
import com.fol.Messages.senderKey
import com.fol.Messages.timestamp
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.websocket.Frame
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureRest() {

    routing {
        authenticate("auth-jwt") {

            //send a message
            post ("/send") {
                val messages = call.receive<List<SendMessageRequest>>()
                val createdIds = mutableSetOf<Int>()
                messages.forEach { request ->
                    val genId = try {
                        val messageId = transaction {
                            Messages.insertAndGetId {
                                it[senderKey] = request.senderKey
                                it[receiverKey] = request.receiverKey
                                it[message] = request.message
                                it[timestamp] = request.timestamp
                            }
                        }

                        //send a web socket live message if the target user is connected
                        sessions[request.receiverKey]?.let {
                            val messageRealTime = AppMessage(
                                request.senderKey,
                                request.receiverKey,
                                request.message,
                                request.timestamp,
                                messageId.value
                            )
                            it.outgoing.send(Frame.Text(Json.encodeToString(WsMessage(messageRealTime))))
                        }
                        messageId.value
                    } catch (e: Exception) {
                        -1
                    }
                    createdIds.add(genId)
                }
                //send the caller the message id
                call.respond(SendMessageResponse(createdIds.toList()))
            }

            //a user asks for all the messages sent to them
            get ("/messages") {
                val principal = call.principal<JWTPrincipal>()
                val publicKey = principal!!.payload.getClaim("publicKey").asString()

                val messages = transaction {
                    Messages.select { receiverKey eq publicKey }.map {
                        AppMessage(
                            it[senderKey],
                            it[receiverKey],
                            it[message],
                            it[timestamp],
                            it[Messages.id].value
                        )
                    }
                }

                call.respond(GetMessageResponse(messages))
            }

            //a user tells the server they correctly got a list of messages
            post ("/received") {
                val request = call.receive<MessageReceivedRequest>()

                val messages = transaction {
                    Messages.select { Messages.id inList request.messageIds }.map { Pair(it[senderKey], it[Messages.id].value) }
                }
                println("messages before delete: $messages")
                transaction {
                    Messages.deleteWhere { Messages.id inList request.messageIds }
                }

                //for every message we delete, we send the original sender a message, in case they are connected!
                messages.forEach { message ->
                    val receiverKey = message.first
                    val id = message.second
                    sessions[receiverKey]?.let {
                        it.outgoing.send(
                            Frame.Text(
                                Json.encodeToString(
                                    WsDelivery(listOf(id))
                                )
                            )
                        )
                    }
                }

                call.respond(MessageReceivedResponse(true))
            }

            //check if a list of messages are delivered, return the list of the delivered only, so we can delete them locally
            post ("/check") {
                val request = call.receive<DeliveryCheckRequest>()
                val delivered = transaction {
                    val notDelivered = Messages.select{ Messages.id inList request.messageIds }.map { it[Messages.id].value }
                    request.messageIds.filter { !notDelivered.contains(it) }
                }
                call.respond(DeliveryCheckResponse(delivered))
            }

        }
    }
}

@Serializable
data class DeliveryCheckRequest(val messageIds: List<Int>)

@Serializable
data class DeliveryCheckResponse(val deliveredId: List<Int>)

@Serializable
data class MessageReceivedRequest(val messageIds: List<Int>)

@Serializable
data class MessageReceivedResponse(val success: Boolean)

@Serializable
data class SendMessageRequest(val senderKey: String, val receiverKey: String, val message: String, val timestamp: Long)

@Serializable
data class SendMessageResponse(val remoteId: List<Int>)

@Serializable
data class GetMessageResponse(val remoteId: List<AppMessage>)

@Serializable
data class AppMessage(val senderKey: String, val receiverKey: String, val message: String, val timestamp: Long, val remoteId: Int)

@Serializable
data class WsMessage(val message: AppMessage)

@Serializable
data class WsDelivery(val deliveredId: List<Int>)
