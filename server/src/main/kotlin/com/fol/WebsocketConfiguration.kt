package com.fol

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()

fun Application.configureWebSocket() {
    install(WebSockets) {
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        authenticate("auth-jwt") {
            webSocket("/fol") {
                val principal = call.principal<JWTPrincipal>()
                val publicKey = principal!!.payload.getClaim("publicKey").asString()

                println("Connected value from principal: $publicKey")

                launch {
                    while (isActive) {
                        delay(60000) // Check every minute
                        if (isTokenExpired(principal)) {
                            send(Frame.Text("SessionExpired"))
                            close(CloseReason(CloseReason.Codes.NORMAL, "Session expired"))
                            return@launch
                        }
                    }
                }

                var user: String? = null
                try {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                val request = Json.decodeFromString<WsRequest>(text)

                                when (request.type) {
                                    "connect" -> { //handshake, cache this session
                                        user = request.data["senderKey"]!!
                                        sessions[user] = this
                                    }
                                    else -> {
                                        println("Unexpected frame received: $text")
                                    }
                                }
                            }
                            else -> {
                                println("Unexpected frame received: ${frame::class}")
                            }
                        }
                    }
                } finally {
                    user?.let { sessions.remove(it) }
                }
            }
        }
    }
}

fun isTokenExpired(principal: JWTPrincipal): Boolean {
    val expiration = principal.expiresAt?.toInstant() ?: return true
    return Instant.now().isAfter(expiration)
}

@Serializable
data class WsRequest(val type: String, val data: Map<String, String>)
