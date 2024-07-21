package com.fol.com.fol.crypto

import com.fol.crypto.AppEncryption
import com.fol.crypto.UserKeys

object CryptoManager {

    private val appEncryption = AppEncryption()

    suspend fun generateKeyPair(): UserKeys {
        return appEncryption.generateKeyPair()
    }

    suspend fun validatePublicKey(publicKey: String): Boolean {
        return appEncryption.validatePublicKey(publicKey)
    }

    suspend fun validatePrivateKey(privateKey: String): Boolean {
        return appEncryption.validatePrivateKey(privateKey)
    }

    suspend fun signChallenge(challenge: String, privateKeyBase64: String): String {
        return appEncryption.signChallenge(challenge, privateKeyBase64)
    }

}
