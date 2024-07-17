package com.fol.com.fol.network

import co.touchlab.kermit.Logger
import com.fol.com.fol.model.repo.MessageEventReceiver
import com.fol.com.fol.model.repo.MessagesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface NetworkOperations {
    suspend fun getMessages(): GetMessageResponse
    suspend fun received(messages: MessageReceivedRequest) : MessageReceivedResponse
    suspend fun check(messages: DeliveryCheckRequest) : DeliveryCheckResponse
    suspend fun sendMessages(messages: List<SendMessageRequest>) : SendMessageResponse
    suspend fun connect()
    suspend fun disconnect()
}

class FakeNetwrok : NetworkOperations{
    override suspend fun getMessages(): GetMessageResponse {
        return GetMessageResponse(messages = emptyList())
    }

    override suspend fun received(messages: MessageReceivedRequest): MessageReceivedResponse {
        return MessageReceivedResponse(true)
    }

    override suspend fun check(messages: DeliveryCheckRequest): DeliveryCheckResponse {
        return DeliveryCheckResponse(emptyList())
    }

    override suspend fun sendMessages(messages: List<SendMessageRequest>): SendMessageResponse {
        return SendMessageResponse(emptyList())
    }

    override suspend fun connect() {

    }

    override suspend fun disconnect() {

    }

}



class NetworkManager(
    private val client: HttpClient,
    private val messageEventReceiver: MessageEventReceiver
    ) : NetworkOperations {

    companion object {
        const val SERVER_URL = "192.168.0.2"
        const val PORT = 8182
    }

    private var session: DefaultClientWebSocketSession? = null

//    val connected = MutableStateFlow(false)
//    val messages = MutableStateFlow<ServerEvent<*>?>(null)

    override suspend fun connect() {

        Logger.i(">> WS CONNECT")
        client.webSocket(method = HttpMethod.Get, port = PORT, host = SERVER_URL, path = "/fol") {

            session = this // Store the session

            // Send a message
            send(Json.encodeToString(WsRequest("connect")))
            Logger.i(">> WS handshake sent")


            // Receive messages
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {


                        try {
                            val data = Json.decodeFromString<WsData>(frame.readText())
                            when (data.type) {
                                "message" -> {
                                    val message = data.toWsMessage()
                                    messageEventReceiver.addMessageFromServer(message.message)
                                    println("Received WsMessage: ${message.message}")
                                }
                                "delivery" -> {
                                    val delivery = data.toWsDelivery()
                                    messageEventReceiver.gotDeliveryFromServer(delivery.deliveredId)
                                    println("Received WsDelivery: ${delivery.deliveredId}")
                                }
                            }
                        } catch (e: Exception) {
                            println("Error deserializing message: ${e.message}")
                        }

                        Logger.i(">> Received text: ${frame.readText()}")
                    }
                    is Frame.Binary -> Logger.i(">> Received binary: ${frame.readBytes()}")
                    else -> Logger.i(">> Received unknown frame: $frame")
                }
            }
        }

    }

    override suspend fun disconnect() {
        Logger.i(">> WS DISCONNECT")
        session?.close(CloseReason(CloseReason.Codes.NORMAL, ""))
        client.close()
    }

    suspend fun serverStatus() {
        val appStatusResponse: ServerStatusResponse = client.get("http://$SERVER_URL:$PORT/serverStatus").body()
        Logger.i("appStatusResponse: $appStatusResponse")
    }

    override suspend fun sendMessages(messages: List<SendMessageRequest>) : SendMessageResponse {
        val sendMessageResponse: SendMessageResponse = client.post("http://$SERVER_URL:$PORT/send"){
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(messages)
        }.body()
        Logger.i("sendMessages sendMessageResponse: $sendMessageResponse")
        return sendMessageResponse
    }

    override suspend fun getMessages()  : GetMessageResponse{
        val getMessageResponse: GetMessageResponse = client.get("http://$SERVER_URL:$PORT/messages").body()
        Logger.i("getMessages getMessageResponse: $getMessageResponse")
        return getMessageResponse
    }

    override suspend fun received(messages: MessageReceivedRequest) : MessageReceivedResponse {
        val messageReceivedResponse: MessageReceivedResponse = client.post("http://$SERVER_URL:$PORT/received"){
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(messages)
        }.body()
        Logger.i("received messageReceivedResponse: $messageReceivedResponse")
        return messageReceivedResponse
    }

    override suspend fun check(messages: DeliveryCheckRequest) : DeliveryCheckResponse {
        val deliveryCheckResponse: DeliveryCheckResponse = client.post("http://$SERVER_URL:$PORT/check"){
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(messages)
        }.body()
        Logger.i("check deliveryCheckResponse: $deliveryCheckResponse")
        return deliveryCheckResponse
    }

}
