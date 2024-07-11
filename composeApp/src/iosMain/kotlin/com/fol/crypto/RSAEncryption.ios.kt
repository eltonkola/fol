package com.fol.crypto

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*
import platform.CoreFoundation.*

actual class RSAEncryption actual constructor() {
    actual suspend fun encryptWithPublicKey(publicKeyBase64: String, message: String): String {
        val publicKeyData = NSData.create(base64EncodedString = publicKeyBase64, options = NSDataBase64DecodingOptions.NSDataBase64DecodingIgnoreUnknownCharacters)
        val keyDict = mapOf(
            kSecAttrKeyType to kSecAttrKeyTypeRSA,
            kSecAttrKeyClass to kSecAttrKeyClassPublic,
            kSecAttrKeySizeInBits to 2048
        )
        var error: NSError? = null
        val publicKey = SecKeyCreateWithData(publicKeyData, keyDict as CFDictionary, error.ptr)

        val messageData = message.encodeToByteArray()
        val algorithm = SecKeyAlgorithm.RSAES_OAEP
        val encryptedData = SecKeyCreateEncryptedData(publicKey, algorithm, messageData as CFData, error.ptr)

        return (encryptedData as NSData).base64EncodedStringWithOptions(0)
    }

    actual suspend fun decryptWithPrivateKey(privateKeyBase64: String, encryptedMessageBase64: String): String {
        val privateKeyData = NSData.create(base64EncodedString = privateKeyBase64, options = NSDataBase64DecodingOptions.NSDataBase64DecodingIgnoreUnknownCharacters)
        val keyDict = mapOf(
            kSecAttrKeyType to kSecAttrKeyTypeRSA,
            kSecAttrKeyClass to kSecAttrKeyClassPrivate,
            kSecAttrKeySizeInBits to 2048
        )
        var error: NSError? = null
        val privateKey = SecKeyCreateWithData(privateKeyData, keyDict as CFDictionary, error.ptr)

        val encryptedData = NSData.create(base64EncodedString = encryptedMessageBase64, options = NSDataBase64DecodingOptions.NSDataBase64DecodingIgnoreUnknownCharacters)
        val algorithm = SecKeyAlgorithm.RSAES_OAEP
        val decryptedData = SecKeyCreateDecryptedData(privateKey, algorithm, encryptedData as CFData, error.ptr)

        return NSString.create(data = decryptedData as NSData, encoding = NSUTF8StringEncoding) as String
    }

    actual suspend fun signWithPrivateKey(privateKeyBase64: String, message: String): String {
        val privateKeyData = NSData.create(base64EncodedString = privateKeyBase64, options = NSDataBase64DecodingOptions.NSDataBase64DecodingIgnoreUnknownCharacters)
        val keyDict = mapOf(
            kSecAttrKeyType to kSecAttrKeyTypeRSA,
            kSecAttrKeyClass to kSecAttrKeyClassPrivate,
            kSecAttrKeySizeInBits to 2048
        )
        var error: NSError? = null
        val privateKey = SecKeyCreateWithData(privateKeyData, keyDict as CFDictionary, error.ptr)

        val messageData = message.encodeToByteArray()
        val algorithm = SecKeyAlgorithm.RSASignatureMessagePKCS1v15SHA256
        val signature = SecKeyCreateSignature(privateKey, algorithm, messageData as CFData, error.ptr)

        return (signature as NSData).base64EncodedStringWithOptions(0)
    }

    actual suspend fun verifyWithPublicKey(publicKeyBase64: String, message: String, signature: String): Boolean {
        val publicKeyData = NSData.create(base64EncodedString = publicKeyBase64, options = NSDataBase64DecodingOptions.NSDataBase64DecodingIgnoreUnknownCharacters)
        val keyDict = mapOf(
            kSecAttrKeyType to kSecAttrKeyTypeRSA,
            kSecAttrKeyClass to kSecAttrKeyClassPublic,
            kSecAttrKeySizeInBits to 2048
        )
        var error: NSError? = null
        val publicKey = SecKeyCreateWithData(publicKeyData, keyDict as CFDictionary, error.ptr)

        val messageData = message.encodeToByteArray()
        val signatureData = NSData.create(base64EncodedString = signature, options = NSDataBase64DecodingOptions.NSDataBase64DecodingIgnoreUnknownCharacters)
        val algorithm = SecKeyAlgorithm.RSASignatureMessagePKCS1v15SHA256

        return SecKeyVerifySignature(publicKey, algorithm, messageData as CFData, signatureData as CFData, error.ptr)
    }

    actual suspend fun generateKeyPair(): Pair<String, String> {
        val parameters = mapOf(
            kSecAttrKeyType to kSecAttrKeyTypeRSA,
            kSecAttrKeySizeInBits to 2048
        )
        var error: NSError? = null
        val keyPair = SecKeyGeneratePair(parameters as CFDictionary, error.ptr)

        val publicKeyData = SecKeyCopyExternalRepresentation(keyPair.first, error.ptr)
        val privateKeyData = SecKeyCopyExternalRepresentation(keyPair.second, error.ptr)

        val publicKeyBase64 = (publicKeyData as NSData).base64EncodedStringWithOptions(0)
        val privateKeyBase64 = (privateKeyData as NSData).base64EncodedStringWithOptions(0)

        return Pair(publicKeyBase64, privateKeyBase64)
    }
}