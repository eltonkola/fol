package com.fol

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        configureSecurityRouting()
        configureWebSocket()
        configureRest()
        configureDatabase()
        legacyWs()

}
