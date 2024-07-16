package com.fol

import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*

fun generateChallenge(length: Int = 32): String {
    val bytes = ByteArray(length)
    Random().nextBytes(bytes)
    return Base64.getEncoder().encodeToString(bytes)
}

fun verifySignature(publicKeyString: String, challenge: String, signatureString: String): Boolean {
    try {
        val keyBytes = Base64.getDecoder().decode(publicKeyString)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(keySpec)

        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(publicKey)
        signature.update(challenge.toByteArray())

        val signatureBytes = Base64.getDecoder().decode(signatureString)
        return signature.verify(signatureBytes)
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}
