package com.fol.network

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.websocket.*

actual fun createHttpClient(): HttpClient {
    return HttpClient(Js) {
        install(WebSockets)
    }
}