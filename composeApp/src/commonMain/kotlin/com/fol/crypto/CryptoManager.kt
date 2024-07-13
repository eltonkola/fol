package com.fol.com.fol.crypto

import com.fol.crypto.RSAEncryption
import com.fol.crypto.UserKeys
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
    suspend fun generateKeyPair(): UserKeys {
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

    suspend fun testValidity(){
        val keys = rsaEncryption.generateKeyPair()

        val myKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCxW/jyXY4Yh5Uph0woLnB3Q6REa+/Npyy3oUWRDk+hA9BHZv8kjFotUOBurcMc/BVhXG7jBUeK5rxDSgmezdG801RXE1f1KmVh1JrwhjKICRNuhD+d4D1AxW99oXorT+8U3AvwTd3toNSyaReupi4mYW0xLns9YUfzJV0pg2NCfIQJpweZqnE+9H5s/f5zGrZtVf864afHbGso/gg32SQ8hSdHtGgxvnut8v9H+haxqToY4SyTj7qZM1AJM4ILF8CvWzR8DjiYcsbVDacmOZE/sZVejRmN6ICtwXdZOd/i2+C0byd+TQ02TMF95KdgQmIArMuJ3S+rcLyEKII6PVHTAgMBAAECggEAQvMn9z1vnTzZBrMnAWf+Vlb3VEi2XhhEHhFHtGkFkA9jeQCdQYulosOL5Nu88l0KKRV+Wj4s8ncFDBWPqh2hlbjkdnY71rUpThO2ZSMTQhzC9A4CAbObC6871d21w1HKO0KRUyXhp4j1oV9gbDIY664NTOpx5Qqq4VLyZvBM8NR09/nvW3kfzP+LIbwhTzyUVkJMUSbIrC2GxonIAH/ZIGP5C+CA6qQaGDfqXEPcZQZLkGjg8Eklqu6UKgNS7HRYoMCKIJbPs97knwZRdKP9m1Z90ZbrKV0eknMqe9ZnnLxeWUVCwqd5ew5KN0MzO8XFAiNY/wrAVfXywqcK9mV6AQKBgQDdq33dTcjsZFgJd4Soaf2mlUJIFV+mfpwpM65Q8a5uE9C145dI1/vRG2m++w+gMeaY6BItZTx1NGRLoWIxnYIelR6JrMn5ldEkVlAYw1K5z+I7lpAZF5DoVYvdwvjkqHCsbfltmRgZYAj+dyBRFDol5cfy/9FeTQKjtzNYdwBM0wKBgQDM07SCJaEuseil6ZzHEt90QLBmPa4HAd2GOTrpKLfHomWOK6Yx1Knv7pTwPwbJ+lNZ5VCqFl5ipZhbn3wpLiX01YDske82nGhH5h4xBtyYLQMsCsRRXuFZScGBjGSwEah2s/eAIizOpAG1H5Eemqq2JpVhxVUpKiDPb+uoQ0fHAQKBgQDNaS73Ni9sLwgNrb02OxGbnlCPb471P/oBjCyKQYr74kzTYBU+oTZubVKucQPn1JX1N4gwnNFLYsNV+jR9+ZEsOc7UkVOQfq4RWBJdABLWsLbZtQnO1rGnOi6jzxrI+PWAM2ChmO5mBUIQW9+Mgovntmh0223uTrqVbxyba5rRlQKBgFEaatk1yTLSI0q+y+NPp8dCbhatyaDgrjSdqKP2CX804H2A7xfAG2Hz2GmgqzGCu0pXMK+Il+r7ou50ohc0PLdLavdBebToDaqahc3n56Uh/aWMxdtqdFSEjeicDKpi84+9Bv1nitJvwnVTgITbvUjO0kuadXQNSFya9nbz3RIBAoGAe2GbwNey3LOx2Q4z9Ql/+RXspScgSfxv+bR2k+rEq3rYoKQDSMH14rVvpmgT0qNZpkW+C55J//9EihKTuK7KMbEC8lHOQqoG/tUJjWO5rJybQzO1UXhT71pybkNF0yvC0caYfMmaLl+n2Fl6tC1fSRBp64GOAyzBMxhicDU93J0="
        println("senderPublicKey: ${keys.publicKey}")
        println("Valid: ${validatePublicKey(keys.publicKey)}")

        println("myKey: $myKey")
        println("Valid: ${validatePublicKey(myKey)}")
    }

    suspend fun validatePublicKey(publicKey: String): Boolean {
        return rsaEncryption.validatePublicKey(publicKey)
    }

}
