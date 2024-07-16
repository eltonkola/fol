package com.fol

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

fun Application.configureDatabase() {
    Database.connect("jdbc:sqlite:./data.db", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(Messages)
    }

}

// Database tables
object Messages : IntIdTable() {
    val senderKey = text("senderKey")
    val receiverKey = text("receiverKey")
    val message = text("message")
    val timestamp = long("timestamp")
}