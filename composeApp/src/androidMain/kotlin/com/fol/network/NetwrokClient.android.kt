package com.fol.network

import com.fol.com.fol.network.BearerAuth
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


actual fun createSecureHttpClient(tokenProvider: () -> String): HttpClient {
    return HttpClient(Android) {
        install(WebSockets)
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(BearerAuth) {
            this.tokenProvider = tokenProvider
        }
//        install(Logging) {
//            level = LogLevel.BODY
//        }
    }
}

actual fun createHttpClient(): HttpClient {
    return HttpClient(Android) {
        install(WebSockets)
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
}


