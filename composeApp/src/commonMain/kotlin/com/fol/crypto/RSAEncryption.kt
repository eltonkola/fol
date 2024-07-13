package com.fol.crypto

expect class RSAEncryption() {
    suspend fun encryptWithPublicKey(publicKeyBase64: String, message: String): String
    suspend fun decryptWithPrivateKey(privateKeyBase64: String, encryptedMessageBase64: String): String
    suspend fun signWithPrivateKey(privateKeyBase64: String, message: String): String
    suspend fun verifyWithPublicKey(publicKeyBase64: String, message: String, signature: String): Boolean
    suspend fun generateKeyPair(): UserKeys
    suspend fun validatePublicKey(publicKeyBase64: String): Boolean
}

data class UserKeys(
    val publicKey: String,
    val privateKey: String
)