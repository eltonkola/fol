package com.fol.com.fol.crypto

import com.fol.crypto.RSAEncryption
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SecureMessage(
    val id: String,
    val timestamp: Instant,
    val content: String
)


object CryptoManager {

    private val rsaEncryption = RSAEncryption()

    //generate a user profile
    suspend fun generateKeyPair(): Pair<String, String> {
        return rsaEncryption.generateKeyPair()
    }

//    fun createProfile() {
//        val keys = generateKeyPair()
//
//        println("publicKey: ${keys.first}")
//        println("secretKey: ${keys.second}")
//
//    }

    suspend fun test() {


        // Generate key pairs for both sender and recipient
        val (senderPublicKey, senderPrivateKey) = rsaEncryption.generateKeyPair()
        val (recipientPublicKey, recipientPrivateKey) = rsaEncryption.generateKeyPair()

        val originalMessage = SecureMessage(
            id = "0001", //UUID.randomUUID().toString(),
            timestamp = Clock.System.now(),
            content = "Hello, this is a secure message!"
        )

        val messageJson = Json.encodeToString(originalMessage)


        // Sender's process
        val signature = rsaEncryption.signWithPrivateKey(senderPrivateKey, messageJson)
        val messageToSend = "$messageJson|$signature"
        val encryptedMessage = rsaEncryption.encryptWithPublicKey(recipientPublicKey, messageToSend)

        println("Encrypted message to send: $encryptedMessage")

        // Recipient's process
        val decryptedMessage =
            rsaEncryption.decryptWithPrivateKey(recipientPrivateKey, encryptedMessage)
        val (receivedMessageJson, receivedSignature) = decryptedMessage.split("|")

        val isSignatureValid = rsaEncryption.verifyWithPublicKey(
            senderPublicKey,
            receivedMessageJson,
            receivedSignature
        )

        if (isSignatureValid) {
            val receivedMessage = Json.decodeFromString<SecureMessage>(receivedMessageJson)
            println("Received message: ${receivedMessage.content}")
            println("Message ID: ${receivedMessage.id}")
            println("Timestamp: ${receivedMessage.timestamp}")
        } else {
            println("Message signature is invalid!")
        }
    }

}
