package com.fol

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
data class GetMessageResponse(val remoteId: List<ServerMessage>)

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

fun WsDelivery.toWsData() : WsData {
    return WsData(
        type = "delivery",
        data = mapOf(
            "ids" to this.deliveredId.joinToString(",")
        )
    )
}

fun WsMessage.toWsMessage() : WsData {
    return WsData(
        type = "message",
        data = mapOf(
            "senderKey" to this.message.senderKey,
            "receiverKey" to this.message.receiverKey,
            "message" to this.message.message,
            "timestamp" to this.message.timestamp.toString(),
            "remoteId" to this.message.remoteId.toString()
        )
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


