package com.fol.crypto

import dev.whyoleg.cryptography.*
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.operations.signature.*
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() = runBlocking{
    val rsaEncryption = RSAEncryption()
    val pair = rsaEncryption.generateKeyPair()
    println("public key: ${pair.publicKey}")
    println("private key: ${pair.privateKey}")
    val text = "Ku je shqipe!"
    val encrypted = rsaEncryption.encryptWithPublicKey(pair.publicKey, text)
    println("encrypted: $encrypted")
    val decrypted = rsaEncryption.decryptWithPrivateKey(pair.privateKey, encrypted)
    println("decrypted: $decrypted")

    println("valid public key: ${rsaEncryption.validatePublicKey(pair.publicKey)}")
    println("invalid public key: ${rsaEncryption.validatePublicKey("kot")}")

    println("valid private key: ${rsaEncryption.validatePrivateKey(pair.privateKey)}")
    println("invalid private key: ${rsaEncryption.validatePrivateKey("kot")}")
}

class RSAEncryption {

    private val provider = CryptographyProvider.Default
    private val rsa = provider.get(RSA.OAEP)

    suspend fun generateKeyPair(): UserKeys {
        val keyGenerator = rsa.keyPairGenerator()
        val keyPair = keyGenerator.generateKey()
        val publicKey = keyPair.publicKey.encodeTo(RSA.PublicKey.Format.DER).encodeBase64()
        val privateKey = keyPair.privateKey.encodeTo(RSA.PrivateKey.Format.DER).encodeBase64()
        return UserKeys(publicKey, privateKey)
    }

    suspend fun encryptWithPublicKey(publicKeyBase64: String, message: String): String = withContext(Dispatchers.Default) {
        val publicKeyBytes = publicKeyBase64.decodeBase64Bytes()
        val publicKeyDecoder = rsa.publicKeyDecoder(SHA256)
        val publicKey = publicKeyDecoder.decodeFrom(RSA.PublicKey.Format.DER, publicKeyBytes)
        val encryptor = publicKey.encryptor()
        val encryptedBytes = encryptor.encryptBlocking(message.encodeToByteArray())
        encryptedBytes.encodeBase64()
    }

    suspend fun decryptWithPrivateKey(privateKeyBase64: String, encryptedMessageBase64: String): String = withContext(Dispatchers.Default) {
        val privateKeyBytes = privateKeyBase64.decodeBase64Bytes()
        val privateKeyDecoder = rsa.privateKeyDecoder(SHA256)
        val privateKey = privateKeyDecoder.decodeFrom(RSA.PrivateKey.Format.DER, privateKeyBytes)
        val decryptor = privateKey.decryptor()
        val encryptedBytes = encryptedMessageBase64.decodeBase64Bytes()
        val decryptedBytes = decryptor.decryptBlocking(encryptedBytes)
        decryptedBytes.decodeToString()
    }

    suspend fun signWithPrivateKey(privateKeyBase64: String, message: String): String = withContext(Dispatchers.Default) {
//        val privateKeyBytes = privateKeyBase64.decodeBase64Bytes()
//        val privateKey = rsa.privateKeyDecoder(SHA256).decodeFrom(RSA.PrivateKey.Format.DER, privateKeyBytes)
//
//        val signatureGenerator = privateKey.signatureGenerator(SHA256)
//        val signatureBytes = signatureGenerator.generateSignature(message.encodeToByteArray())
//        signatureBytes.encodeBase64()

        val privateKeyBytes = privateKeyBase64.decodeBase64Bytes()
        val privateKeyDecoder = rsa.privateKeyDecoder(SHA256)
        val privateKey = privateKeyDecoder.decodeFrom(RSA.PrivateKey.Format.DER, privateKeyBytes)



        // Sign the message
        val signer = rsa.sig(RSA.PKCS1(SHA256))
        val signatureBytes = signer.sign(privateKey, message.encodeToByteArray())

        // Encode the signature to Base64
        return signatureBytes.encodeTo(Base64)


        val signatureGenerator = privateKey.signatureGenerator(SHA256)
        val signatureBytes = signatureGenerator.generateSignature(message.encodeToByteArray())
        signatureBytes.encodeBase64()


        return rsa.verifier(RSA.PKCS1(SHA256)).verify(publicKey, message.encodeToByteArray(), signatureBytes)

         ""
    }

    suspend fun verifyWithPublicKey(publicKeyBase64: String, message: String, signatureBase64: String): Boolean = withContext(Dispatchers.Default) {
//        val publicKeyBytes = publicKeyBase64.decodeBase64Bytes()
//        val publicKeyDecoder = rsa.publicKeyDecoder(SHA256)
//        val publicKey = publicKeyDecoder.decodeFrom(RSA.PublicKey.Format.DER, publicKeyBytes)
//        val signatureBytes = signatureBase64.decodeBase64Bytes()
//        val verifier = publicKey.verifier()
//        verifier.verifyBlocking(message.encodeToByteArray(), signatureBytes)

        true
    }
    suspend fun validatePublicKey(publicKeyBase64: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val publicKeyBytes = publicKeyBase64.decodeBase64Bytes()
            val publicKeyDecoder = rsa.publicKeyDecoder(SHA256)
            publicKeyDecoder.decodeFrom(RSA.PublicKey.Format.DER, publicKeyBytes)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun validatePrivateKey(privateKeyBase64: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val privateKeyBytes = privateKeyBase64.decodeBase64Bytes()
            val privateKeyDecoder = rsa.privateKeyDecoder(SHA256)
            privateKeyDecoder.decodeFrom(RSA.PrivateKey.Format.DER, privateKeyBytes)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signChallenge(challenge: String, privateKeyBase64: String): String {
        return signWithPrivateKey(privateKeyBase64, challenge)
    }
}

data class UserKeys(
    val publicKey: String,
    val privateKey: String
)
