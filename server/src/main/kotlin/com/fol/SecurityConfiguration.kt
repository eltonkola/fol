package com.fol

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

val challenges = ConcurrentHashMap<String, String>()

@Serializable
data class AuthRequest(val publicKey: String)

@Serializable
data class VerifyRequest(val publicKey: String, val signature: String)

fun generateToken(publicKey: String, algorithm: Algorithm): String {
    return JWT.create()
        .withClaim("publicKey", publicKey)
        .withExpiresAt(Date(System.currentTimeMillis() + 3600000)) // 1 hour
        .sign(algorithm)
}

fun Application.configureSecurityRouting() {

    val jwtSecret = environment.config.property("jwt.secret").getString()
    val algorithm = Algorithm.HMAC256(jwtSecret)

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JWT.require(algorithm).build())
            validate { credential ->
                if (credential.payload.getClaim("publicKey").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    routing {
        post ("/auth") {
            val request = call.receive<AuthRequest>()
            val challenge = generateChallenge()
            challenges[request.publicKey] = challenge
            call.respond(mapOf("challenge" to challenge))
        }

        post("/verify") {
            val request = call.receive<VerifyRequest>()
            val storedChallenge = challenges[request.publicKey]
            if (storedChallenge != null && verifySignature(request.publicKey, storedChallenge, request.signature)) {
                val token = generateToken(request.publicKey, algorithm)
                call.respond(mapOf("token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        authenticate("auth-jwt") {
            get("/serverStatus") {
                val principal = call.principal<JWTPrincipal>()
                val publicKey = principal!!.payload.getClaim("publicKey").asString()
                val serverVersion = AppStatusResponse(
                    message = "Fol server online!",
                    publicKey = publicKey,
                    serverVersion = "1.0.0"
                )
                call.respond(serverVersion)
            }
        }

    }
}

@Serializable
data class AppStatusResponse(val message: String, val publicKey: String, val serverVersion: String)