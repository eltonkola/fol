package com.fol.crypto

import dev.whyoleg.cryptography.*
import dev.whyoleg.cryptography.algorithms.asymmetric.EC
import dev.whyoleg.cryptography.algorithms.asymmetric.ECDSA
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.symmetric.AES
import dev.whyoleg.cryptography.algorithms.symmetric.SymmetricKeySize
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import dev.whyoleg.cryptography.algorithms.digest.SHA512

fun main() = runBlocking {
    val appEncryption = AppEncryption()
    val sender = appEncryption.createProfile()
    println(">>>>>>>>>> 1.User Registration: >>>>>>>>>")

    println(">>>>>>>>>> sender >>>>>>>>>")
    println("sender ecdsaPublicKey: ${sender.ecdsaPublicKey}")
    println("sender ecdsaPrivateKey: ${sender.ecdsaPrivateKey}")
    println("sender rsaPublicKey: ${sender.rsaPublicKey}")
    println("sender rsaPrivateKey: ${sender.rsaPrivateKey}")

    val receiver = appEncryption.createProfile()
    println(">>>>>>>>>> receiver >>>>>>>>>")
    println("receiver ecdsaPublicKey: ${receiver.ecdsaPublicKey}")
    println("receiver ecdsaPrivateKey: ${receiver.ecdsaPrivateKey}")
    println("receiver rsaPublicKey: ${receiver.rsaPublicKey}")
    println("receiver rsaPrivateKey: ${receiver.rsaPrivateKey}")

    println(">>>>>>>>>> 2.Starting a Conversation: >>>>>>>>>")
    val sessionKey = appEncryption.createSessionKey(receiver.rsaPublicKey)
    println("signed sessionKey: $sessionKey")
    println(">>>>>>>>>> 3. Sending Messages: >>>>>>>>>")
    val message = "Ku je shqipe!"
    println("typed message: $message")
    val encrypted = appEncryption.encryptAndSign(sessionKey, message, receiver.ecdsaPrivateKey)
    println("encrypted message: ${encrypted.first}")
    println("signature: ${encrypted.second}")
    println(">>>>>>>>>> 4. Receiving Messages: >>>>>>>>>")

    val decrypted = appEncryption.decryptIncomingMessage(
        sessionKey,
        receiver.rsaPrivateKey,
        encrypted.first,
        encrypted.second,
        sender.ecdsaPublicKey
    )

    println("decrypted message: $decrypted")
}

/*
	1.	User Registration: Generate ECC and RSA key pairs for each user.
	2.	Starting a Conversation: Generate an AES session key and encrypt it with the recipient’s RSA public key.
	3.	Sending Messages: Encrypt the message with AES-GCM using the session key and sign the encrypted message with ECDSA.
	4.	Receiving Messages: Decrypt the session key with the recipient’s RSA private key, decrypt the message using AES-GCM, and verify the signature with ECDSA.
Docs:
   https://whyoleg.github.io/cryptography-kotlin/examples/
*/

class AppEncryption {

    private val provider = CryptographyProvider.Default
    private val ecdsa = provider.get(ECDSA)
    private val aesGcm = provider.get(AES.GCM)
    private val rsa = provider.get(RSA.OAEP)


    private suspend fun createRsaKeyPair(): Pair<String, String> =
        withContext(Dispatchers.Default) {
            val rsaKeyGenerator = rsa.keyPairGenerator()
            val rsaKeyPair = rsaKeyGenerator.generateKey()
            val rsaPublicKey =
                rsaKeyPair.publicKey.encodeTo(RSA.PublicKey.Format.DER).encodeBase64()
            val rsaPrivateKey =
                rsaKeyPair.privateKey.encodeTo(RSA.PrivateKey.Format.DER).encodeBase64()
            Pair(rsaPublicKey, rsaPrivateKey)
        }

    private suspend fun createEcdsaKeyPair(): Pair<String, String> =
        withContext(Dispatchers.Default) {
            val ecdsaKeyGenerator = ecdsa.keyPairGenerator(EC.Curve.P521)
            val ecdsaKeyPair = ecdsaKeyGenerator.generateKey()
            val ecdsaPublicKey =
                ecdsaKeyPair.publicKey.encodeTo(ECDSA_PUBLIC_KEY_FORMAT).encodeBase64()
            val ecdsaPrivateKey =
                ecdsaKeyPair.privateKey.encodeTo(ECDSA_PRIVATE_KEY_FORMAT).encodeBase64()
            Pair(ecdsaPublicKey, ecdsaPrivateKey)
        }

    private suspend fun createAesGcm(): String = withContext(Dispatchers.Default) {
        val aesGcmKeyGenerator = aesGcm.keyGenerator(SymmetricKeySize.B256)
        val aesGcmKey: AES.GCM.Key = aesGcmKeyGenerator.generateKey()
        val aesGcmKeyValue = aesGcmKey.encodeToBlocking(AES_KEY_FORMAT).encodeBase64()
        aesGcmKeyValue
    }

    //1.	User Registration: Generate ECC and RSA key pairs for each user.
    //The user profile has a RSA key and a ECDSA key, one for encryption and the other for verification
    suspend fun createProfile(): UserKeys = withContext(Dispatchers.Default) {
        val rsa = createRsaKeyPair()
        val ecdsa = createEcdsaKeyPair()

        UserKeys(
            rsaPublicKey = rsa.first,
            rsaPrivateKey = rsa.second,
            ecdsaPublicKey = ecdsa.first,
            ecdsaPrivateKey = ecdsa.second,
        )
    }


    //2.    Starting a Conversation: Generate an AES session key and encrypt it with the recipient’s RSA public key.
    suspend fun createSessionKey(receiverPublicKeyBase64: String): String =
        withContext(Dispatchers.Default) {
            val aesGcmKeyValue = createAesGcm()
            val sessionKey = encryptWithRsaPublicKey(receiverPublicKeyBase64, aesGcmKeyValue)
            sessionKey
        }


    //3.	Sending Messages: Encrypt the message with AES-GCM using the session key and sign the encrypted message with ECDSA.
    suspend fun encryptAndSign(
        aesSessionKeyBase64: String,
        message: String,
        ecdsaPrivateKeyBase64: String
    ): Pair<String, String> = withContext(Dispatchers.Default) {
        val encrypted = encryptWithEasGcm(aesSessionKeyBase64, message)
        val signature = signWithEcdsa(ecdsaPrivateKeyBase64, encrypted)
        Pair(encrypted, signature)
    }


    //	4.	Receiving Messages: Decrypt the session key with the recipient’s RSA private key, decrypt the message using AES-GCM, and verify the signature with ECDSA.
    suspend fun decryptIncomingMessage(
        sessionKeyBase64: String,
        privateRsaKeyBase64: String,
        signatureBase64: String,
        encryptedMessageBase64: String,
        ecdsaPublicKeyBase64: String
    ): String = withContext(Dispatchers.Default) {
        //decrypt session key
        try {

            val decryptedSessionKey = decryptWithRsaPrivateKey(privateRsaKeyBase64, sessionKeyBase64)
            val decodedSessionKey: AES.GCM.Key = aesGcm.keyDecoder()
                .decodeFrom(AES_KEY_FORMAT, decryptedSessionKey.decodeBase64Bytes())

            val decryptedMessage =
                decodedSessionKey.cipher().decrypt(encryptedMessageBase64.decodeBase64Bytes())


            val ecdsaKey = ecdsa.publicKeyDecoder(EC.Curve.P521)
                .decodeFrom(ECDSA_PUBLIC_KEY_FORMAT, ecdsaPublicKeyBase64.decodeBase64Bytes())

            val verificationResult: Boolean = ecdsaKey.signatureVerifier(digest = SHA512)
                .verifySignature(
                    signatureBase64.encodeToByteArray(),
                    encryptedMessageBase64.decodeBase64Bytes()
                )

            if (!verificationResult) {
                throw Exception("Signature verification failed")
            } else {
                decryptedMessage.decodeToString()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private suspend fun signWithEcdsa(ecdsaPrivateKey: String, encryptedMessage: String): String =
        withContext(Dispatchers.Default) {
            val privateKeyBytes = ecdsaPrivateKey.decodeBase64Bytes()
            val decodedPublicKey: ECDSA.PrivateKey = ecdsa.privateKeyDecoder(EC.Curve.P521)
                .decodeFrom(ECDSA_PRIVATE_KEY_FORMAT, privateKeyBytes)
            val signature: ByteArray = decodedPublicKey.signatureGenerator(digest = SHA512)
                .generateSignature(encryptedMessage.encodeToByteArray())
            signature.decodeToString()
        }


    private suspend fun encryptWithEasGcm(aesKeyBase64: String, message: String): String =
        withContext(Dispatchers.Default) {
            val publicKeyBytes = aesKeyBase64.decodeBase64Bytes()
            val decodedKey = aesGcm.keyDecoder().decodeFrom(AES_KEY_FORMAT, publicKeyBytes)
            val decodedKeyCipher = decodedKey.cipher()
            val messageBytes = message.encodeToByteArray()
            val encrypted = decodedKeyCipher.encrypt(messageBytes).decodeToString()
            encrypted
        }

    suspend fun encryptWithRsaPublicKey(publicKeyBase64: String, message: String): String =
        withContext(Dispatchers.Default) {
            val publicKeyBytes = publicKeyBase64.decodeBase64Bytes()
            val publicKeyDecoder = rsa.publicKeyDecoder(SHA256)
            val publicKey = publicKeyDecoder.decodeFrom(RSA_PUBLIC_KEY_FORMAT, publicKeyBytes)
            val encryptor = publicKey.encryptor()
            val encryptedBytes = encryptor.encryptBlocking(message.encodeToByteArray())
            encryptedBytes.encodeBase64()
        }

    suspend fun decryptWithRsaPrivateKey(
        privateKeyBase64: String,
        encryptedMessageBase64: String
    ): String = withContext(Dispatchers.Default) {
        val privateKeyBytes = privateKeyBase64.decodeBase64Bytes()
        val privateKeyDecoder = rsa.privateKeyDecoder(SHA256)
        val privateKey = privateKeyDecoder.decodeFrom(RSA_PRIVATE_KEY_FORMAT, privateKeyBytes)
        val decryptor = privateKey.decryptor()
        val encryptedBytes = encryptedMessageBase64.decodeBase64Bytes()
        val decryptedBytes = decryptor.decryptBlocking(encryptedBytes)
        decryptedBytes.decodeToString()
    }

    suspend fun validatePublicKey(publicKeyBase64: String): Boolean =
        withContext(Dispatchers.Default) {
            try {
                val publicKeyBytes = publicKeyBase64.decodeBase64Bytes()
                val publicKeyDecoder = rsa.publicKeyDecoder(SHA256)
                publicKeyDecoder.decodeFrom(RSA_PUBLIC_KEY_FORMAT, publicKeyBytes)
                true
            } catch (e: Exception) {
                false
            }
        }

    suspend fun validatePrivateKey(privateKeyBase64: String): Boolean =
        withContext(Dispatchers.Default) {
            try {
                val privateKeyBytes = privateKeyBase64.decodeBase64Bytes()
                val privateKeyDecoder = rsa.privateKeyDecoder(SHA256)
                privateKeyDecoder.decodeFrom(RSA_PRIVATE_KEY_FORMAT, privateKeyBytes)
                true
            } catch (e: Exception) {
                false
            }
        }

    companion object{
        private val RSA_PUBLIC_KEY_FORMAT = RSA.PublicKey.Format.DER
        private val RSA_PRIVATE_KEY_FORMAT = RSA.PrivateKey.Format.DER
        private val ECDSA_PUBLIC_KEY_FORMAT = EC.PublicKey.Format.DER
        private val ECDSA_PRIVATE_KEY_FORMAT = EC.PrivateKey.Format.DER
        private val AES_KEY_FORMAT = AES.Key.Format.RAW
    }
}

data class UserKeys(
    val rsaPublicKey: String,
    val rsaPrivateKey: String,
    val ecdsaPublicKey: String,
    val ecdsaPrivateKey: String,
)
