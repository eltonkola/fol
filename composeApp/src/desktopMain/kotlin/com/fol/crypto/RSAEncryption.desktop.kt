package com.fol.crypto


import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

actual class RSAEncryption actual constructor() {
    private val keyFactory = KeyFactory.getInstance("RSA")
    private val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    private val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
    private val signature = Signature.getInstance("SHA256withRSA")
    private val secureRandom = SecureRandom()

    actual suspend fun encryptWithPublicKey(publicKeyBase64: String, message: String): String {
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64)))

        // Generate a random AES key
        val aesKey = KeyGenerator.getInstance("AES").generateKey()

        // Encrypt the AES key with RSA
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedAesKey = rsaCipher.doFinal(aesKey.encoded)

        // Generate a random IV for AES-GCM
        val iv = ByteArray(12)
        secureRandom.nextBytes(iv)

        // Encrypt the message with AES
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(128, iv))
        val encryptedMessage = aesCipher.doFinal(message.toByteArray())

        // Combine all parts: IV + Encrypted AES Key + Encrypted Message
        val combined = iv + encryptedAesKey + encryptedMessage
        return Base64.getEncoder().encodeToString(combined)
    }

    actual suspend fun decryptWithPrivateKey(privateKeyBase64: String, encryptedMessageBase64: String): String {
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64)))
        val combined = Base64.getDecoder().decode(encryptedMessageBase64)

        // Extract parts
        val iv = combined.sliceArray(0 until 12)
        val encryptedAesKey = combined.sliceArray(12 until 12 + 256) // 2048-bit RSA key produces 256-byte encrypted AES key
        val encryptedMessage = combined.sliceArray(12 + 256 until combined.size)

        // Decrypt the AES key
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
        val aesKey = SecretKeySpec(rsaCipher.doFinal(encryptedAesKey), "AES")

        // Decrypt the message
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, GCMParameterSpec(128, iv))
        val decryptedBytes = aesCipher.doFinal(encryptedMessage)
        return String(decryptedBytes)
    }

    actual suspend fun signWithPrivateKey(privateKeyBase64: String, message: String): String {
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64)))
        signature.initSign(privateKey)
        signature.update(message.toByteArray())
        val signatureBytes = signature.sign()
        return Base64.getEncoder().encodeToString(signatureBytes)
    }

    actual suspend fun verifyWithPublicKey(publicKeyBase64: String, message: String, signature: String): Boolean {
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64)))
        this.signature.initVerify(publicKey)
        this.signature.update(message.toByteArray())
        return this.signature.verify(Base64.getDecoder().decode(signature))
    }

    actual suspend fun generateKeyPair(): UserKeys {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded)
        val privateKey = Base64.getEncoder().encodeToString(keyPair.private.encoded)
        return UserKeys(
            publicKey = publicKey,
            privateKey = privateKey
        )
    }

    actual suspend fun validatePublicKey(publicKeyBase64: String): Boolean {
        return try {
            // Decode the base64 string
            val decoded = Base64.getDecoder().decode(publicKeyBase64)

            // Generate the public key
            val keySpec = X509EncodedKeySpec(decoded)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey = keyFactory.generatePublic(keySpec)

            // Check validity
            publicKey.encoded.isNotEmpty() && publicKey.algorithm == "RSA"
        } catch (e: Exception) {
            // Log the exception for debugging
            println("PublicKeyValidation Validation error: ${e.message}")
            false // If any exception occurs, the key is not valid
        }
    }
}
