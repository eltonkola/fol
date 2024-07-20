package com.fol.com.fol.crypto

import com.fol.crypto.RSAEncryption
import com.fol.crypto.UserKeys

object CryptoManager {

    private val rsaEncryption = RSAEncryption()

    suspend fun generateKeyPair(): UserKeys {
        return rsaEncryption.generateKeyPair()
    }

    suspend fun validatePublicKey(publicKey: String): Boolean {
        return rsaEncryption.validatePublicKey(publicKey)
    }

    suspend fun validatePrivateKey(privateKey: String): Boolean {
        return rsaEncryption.validatePrivateKey(privateKey)
    }

    suspend fun signChallenge(challenge: String, privateKeyBase64: String): String {
        return rsaEncryption.signChallenge(challenge, privateKeyBase64)
    }

}
