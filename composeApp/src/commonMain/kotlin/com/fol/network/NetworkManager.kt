package com.fol.com.fol.network

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.flow.MutableStateFlow

class NetworkManager(private val client: HttpClient) {

    companion object {
        const val SERVER_URL = "192.168.0.2"
        const val PORT = 8282
    }

    private var session: DefaultClientWebSocketSession? = null

    val connected = MutableStateFlow(false)
    val messages = MutableStateFlow<ServerEvent<*>?>(null)

    suspend fun connect() {
        Logger.i(">> WS CONNECT")
        client.webSocket(method = HttpMethod.Get, port = PORT, host = SERVER_URL, path = "/ws") {

            session = this // Store the session

            // Send a message
            send("{\n" +
                    "    \"type\": \"connect\",\n" +
                    "    \"data\" : {\n" +
                    "        \"userId\" : \"2\"\n" +
                    "    }\n" +
                    "}")
            Logger.i(">> WS handshake sent")

            // Receive messages
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> Logger.i(">> Received text: ${frame.readText()}")
                    is Frame.Binary -> Logger.i(">> Received binary: ${frame.readBytes()}")
                    else -> Logger.i(">> Received unknown frame: $frame")
                }
            }
        }

    }

    suspend fun sendMessage(message: String) {
        session?.send(message) ?: Logger.w(">> WS session is not connected")
    }

    suspend fun connect(publicKey: String) {
        send("{ \"type\": \"connect\", \"data\" : { \"senderKey\" : \"$publicKey\" } }")
    }

    private suspend fun send(toSend: String){
        session?.send(toSend) ?: Logger.w(">> WS session is not connected")
    }

    suspend fun disconnect() {
        Logger.i(">> WS DISCONNECT")
        session?.close(CloseReason(CloseReason.Codes.NORMAL, ""))
        client.close()
    }

}
