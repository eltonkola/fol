package com.fol.com.fol.network

import com.fol.com.fol.db.model.AppMessage
import com.fol.com.fol.db.model.toInstant
import kotlinx.serialization.Serializable

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
data class GetMessageResponse(val messages: List<ServerMessage>)

@Serializable
data class ServerMessage(val senderKey: String, val receiverKey: String, val message: String, val timestamp: Long, val remoteId: Int)

@Serializable
data class ServerStatusResponse(val message: String, val publicKey: String, val serverVersion: String)

@Serializable
data class WsRequest(val type: String)


@Serializable
data class WsMessage(val message: ServerMessage)

@Serializable
data class WsDelivery(val deliveredId: List<Int>)

@Serializable
data class WsData(val type: String, val data: Map<String, String>)

fun WsData.toWsDelivery() : WsDelivery {
    return WsDelivery(
        deliveredId = this.data["ids"]?.split(",")?.map { it.toInt() } ?: emptyList()
    )
}

fun WsData.toWsMessage() : WsMessage {
    return WsMessage(
        message = ServerMessage(
            senderKey = this.data["senderKey"] ?: "",
            receiverKey = this.data["receiverKey"] ?: "",
            message = this.data["message"] ?: "",
            timestamp = this.data["timestamp"]?.toLong() ?: 0,
            remoteId = this.data["remoteId"]?.toInt() ?: 0
        )
    )
}

fun AppMessage.toServerMessage(): SendMessageRequest {
    return SendMessageRequest(
        senderKey = this.senderKey,
        receiverKey = this.receiverKey,
        message = this.message,
        timestamp = this.timeSent.toInstant().epochSeconds
    )
}
