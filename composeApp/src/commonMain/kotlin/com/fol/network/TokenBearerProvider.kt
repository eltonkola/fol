package com.fol.com.fol.network

import com.fol.com.fol.crypto.CryptoManager
import com.fol.com.fol.network.NetworkManager.Companion.PORT
import com.fol.com.fol.network.NetworkManager.Companion.SERVER_URL
import com.fol.model.repo.AccountRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.util.AttributeKey

open class BearerAuthConfig {
    var tokenProvider: () -> String = { "" }
}

class BearerAuth(private val config: BearerAuthConfig) {

    class Config : BearerAuthConfig()

    companion object : HttpClientPlugin<Config, BearerAuth> {
        override val key: AttributeKey<BearerAuth> = AttributeKey("BearerAuth")

        override fun prepare(block: Config.() -> Unit): BearerAuth {
            val config = Config().apply(block)
            return BearerAuth(config)
        }

        override fun install(plugin: BearerAuth, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.headers.append("Authorization", "Bearer ${plugin.config.tokenProvider()}")
            }
        }
    }
}

object BearerTokenLoader {

    private const val AUTH_URL = "http://$SERVER_URL:$PORT/auth"
    private const val VERIFY_URL = "http://$SERVER_URL:$PORT/verify"

    suspend fun provideToken(client: HttpClient, accountRepository: AccountRepository): String {

        val authResponse: AuthResponse = client.post(AUTH_URL) {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(accountRepository.currentUser.publicKey))
        }.body()

        val signature = CryptoManager.signChallenge(authResponse.challenge, accountRepository.currentUser.privateKey)

        val verifyResponse: VerifyResponse = client.post(VERIFY_URL) {
            contentType(ContentType.Application.Json)
            setBody(VerifyRequest(accountRepository.currentUser.publicKey, signature))
        }.body()

        client.close()
        return verifyResponse.token
    }
}


@Serializable
data class AuthRequest(val publicKey: String)

@Serializable
data class AuthResponse(val challenge: String)

@Serializable
data class VerifyRequest(val publicKey: String, val signature: String)

@Serializable
data class VerifyResponse(val token: String)

