package com.fol.crypto

import org.khronos.webgl.*
import kotlin.js.Promise
import kotlin.js.json

actual class RSAEncryption actual constructor() {
    private val crypto = js("window.crypto")
    private val subtle = crypto.subtle

    actual suspend fun encryptWithPublicKey(publicKeyBase64: String, message: String): String {
        val publicKey = importPublicKey(publicKeyBase64)
        val messageUint8 = message.encodeToUint8Array()

        val encryptedBuffer = subtle.encrypt(
            json("name" to "RSA-OAEP"),
            publicKey,
            messageUint8
        ).await()

        return encryptedBuffer.toBase64()
    }

    actual suspend fun decryptWithPrivateKey(privateKeyBase64: String, encryptedMessageBase64: String): String {
        val privateKey = importPrivateKey(privateKeyBase64)
        val encryptedUint8 = encryptedMessageBase64.decodeBase64ToUint8Array()

        val decryptedBuffer = subtle.decrypt(
            json("name" to "RSA-OAEP"),
            privateKey,
            encryptedUint8
        ).await()

        return decryptedBuffer.toUint8Array().toKString()
    }

    actual suspend fun signWithPrivateKey(privateKeyBase64: String, message: String): String {
        val privateKey = importPrivateKey(privateKeyBase64)
        val messageUint8 = message.encodeToUint8Array()

        val signatureBuffer = subtle.sign(
            json("name" to "RSASSA-PKCS1-v1_5"),
            privateKey,
            messageUint8
        ).await()

        return signatureBuffer.toBase64()
    }

    actual suspend fun verifyWithPublicKey(publicKeyBase64: String, message: String, signature: String): Boolean {
        val publicKey = importPublicKey(publicKeyBase64)
        val messageUint8 = message.encodeToUint8Array()
        val signatureUint8 = signature.decodeBase64ToUint8Array()

        return subtle.verify(
            json("name" to "RSASSA-PKCS1-v1_5"),
            publicKey,
            signatureUint8,
            messageUint8
        ).await()
    }

    actual suspend fun generateKeyPair(): Pair<String, String> {
        val keyPair = subtle.generateKey(
            json(
                "name" to "RSA-OAEP",
                "modulusLength" to 2048,
                "publicExponent" to Uint8Array(intArrayOf(1, 0, 1)),
                "hash" to "SHA-256"
            ),
            true,
            arrayOf("encrypt", "decrypt")
        ).await()

        val publicKeyJwk = subtle.exportKey("jwk", keyPair.publicKey).await()
        val privateKeyJwk = subtle.exportKey("jwk", keyPair.privateKey).await()

        return Pair(JSON.stringify(publicKeyJwk), JSON.stringify(privateKeyJwk))
    }

    private suspend fun importPublicKey(publicKeyBase64: String): dynamic {
        val keyData = publicKeyBase64.decodeBase64ToUint8Array()
        return subtle.importKey(
            "spki",
            keyData,
            json("name" to "RSA-OAEP", "hash" to "SHA-256"),
            true,
            arrayOf("encrypt")
        ).await()
    }

    private suspend fun importPrivateKey(privateKeyBase64: String): dynamic {
        val keyData = privateKeyBase64.decodeBase64ToUint8Array()
        return subtle.importKey(
            "pkcs8",
            keyData,
            json("name" to "RSA-OAEP", "hash" to "SHA-256"),
            true,
            arrayOf("decrypt")
        ).await()
    }
}

fun ArrayBuffer.toBase64(): String {
    val uint8Array = Uint8Array(this)
    return uint8Array.toBase64()
}

fun Uint8Array.toBase64(): String {
    val binary = buildString {
        for (byte in this@toBase64) {
            append(byte.toChar())
        }
    }
    return js("btoa")(binary) as String
}

fun String.decodeBase64ToUint8Array(): Uint8Array {
    val binaryString = js("atob")(this) as String
    val len = binaryString.length
    val bytes = Uint8Array(len)
    for (i in 0 until len) {
        bytes[i] = binaryString[i].toByte().toInt()
    }
    return bytes
}

fun String.encodeToUint8Array(): Uint8Array {
    val bytes = Uint8Array(length)
    for (i in indices) {
        bytes[i] = this[i].toByte().toInt()
    }
    return bytes
}

fun Uint8Array.toKString(): String {
    return buildString {
        for (byte in this@toKString) {
            append(byte.toChar())
        }
    }
}
}