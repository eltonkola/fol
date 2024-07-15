package com.fol.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.websocket.WebSockets

actual fun createHttpClient(): HttpClient {
    return HttpClient(Darwin) {
        install(WebSockets)
    }
}