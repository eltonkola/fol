package com.fol.com.fol.network

import co.touchlab.kermit.Logger
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

class NetworkManager(
    private val client: HttpClient,
    private val messagesRepository: MessagesRepository
    ) {

    companion object {
        const val SERVER_URL = "192.168.0.2"
        const val PORT = 8182
    }

    private var session: DefaultClientWebSocketSession? = null

//    val connected = MutableStateFlow(false)
//    val messages = MutableStateFlow<ServerEvent<*>?>(null)

    suspend fun connect() {

        Logger.i(">> WS CONNECT")
        client.webSocket(method = HttpMethod.Get, port = PORT, host = SERVER_URL, path = "/ws") {

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
                                    messagesRepository.addMessageFromServer(message.message)
                                    println("Received WsMessage: ${message.message}")
                                }
                                "delivery" -> {
                                    val delivery = data.toWsDelivery()
                                    messagesRepository.gotDeliveryFromServer(delivery.deliveredId)
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

    suspend fun disconnect() {
        Logger.i(">> WS DISCONNECT")
        session?.close(CloseReason(CloseReason.Codes.NORMAL, ""))
        client.close()
    }

    suspend fun serverStatus() {
        val appStatusResponse: ServerStatusResponse = client.get("http://$SERVER_URL:$PORT/serverStatus").body()
        Logger.i("appStatusResponse: $appStatusResponse")
    }

    suspend fun sendMessages(messages: List<SendMessageRequest>) : SendMessageResponse {
        val sendMessageResponse: SendMessageResponse = client.post("http://$SERVER_URL:$PORT/send"){
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(messages)
        }.body()
        Logger.i("sendMessages sendMessageResponse: $sendMessageResponse")
        return sendMessageResponse
    }

    suspend fun getMessages()  : GetMessageResponse{
        val getMessageResponse: GetMessageResponse = client.get("http://$SERVER_URL:$PORT/messages").body()
        Logger.i("getMessages getMessageResponse: $getMessageResponse")
        return getMessageResponse
    }

    suspend fun received(messages: MessageReceivedRequest) : MessageReceivedResponse {
        val messageReceivedResponse: MessageReceivedResponse = client.post("http://$SERVER_URL:$PORT/received"){
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(messages)
        }.body()
        Logger.i("received messageReceivedResponse: $messageReceivedResponse")
        return messageReceivedResponse
    }

    suspend fun check(messages: DeliveryCheckRequest) : DeliveryCheckResponse {
        val deliveryCheckResponse: DeliveryCheckResponse = client.post("http://$SERVER_URL:$PORT/check"){
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(messages)
        }.body()
        Logger.i("check deliveryCheckResponse: $deliveryCheckResponse")
        return deliveryCheckResponse
    }

}
