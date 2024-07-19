package com.fol.crypto

import platform.Foundation.*
import platform.Security.*
import kotlinx.cinterop.*
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFErrorCopyDescription
import platform.CoreFoundation.CFErrorRef
import platform.CoreFoundation.CFErrorRefVar

actual class RSAEncryption {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun encryptWithPublicKey(publicKeyBase64: String, message: String): String {
        val publicKey = loadPublicKey(publicKeyBase64) ?: throw Exception("Invalid public key")
        val messageData = message.encodeToByteArray()

        memScoped {
            val errorPtr = alloc<CFErrorRefVar>()
            val cfData = messageData.toCFData()
            val encryptedData = SecKeyCreateEncryptedData(
                publicKey,
                kSecKeyAlgorithmRSAEncryptionPKCS1,
                cfData,
                errorPtr.ptr
            )

            if (encryptedData == null) {
                val error = errorPtr.value
                throw Exception("Encryption failed: ${CFErrorCopyDescription(error)?.toString()}")
            }

            // Convert CFDataRef to NSData
            val nsData = NSData.dataWithBytes(
                CFDataGetBytePtr(encryptedData),
                CFDataGetLength(encryptedData).toULong()
            )
            return nsData.base64EncodedStringWithOptions(0u)
        }
    }


    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun decryptWithPrivateKey(privateKeyBase64: String, encryptedMessageBase64: String): String {
        val privateKey = loadPrivateKey(privateKeyBase64) ?: throw Exception("Invalid private key")
        val encryptedData = NSData.create(base64EncodedString = encryptedMessageBase64, options = 1U /* NSDataBase64DecodingOptions.IgnoreUnknownCharacters */)
            ?: throw Exception("Invalid base64 encoded message")

        memScoped {
            val errorPtr = alloc<CFErrorRefVar>()
            val cfEncryptedData = encryptedData as CFDataRef
            val decryptedData = SecKeyCreateDecryptedData(
                privateKey,
                kSecKeyAlgorithmRSAEncryptionPKCS1,
                cfEncryptedData,
                errorPtr.ptr
            )

            if (decryptedData == null) {
                val error = errorPtr.value
                throw Exception("Decryption failed: ${CFErrorCopyDescription(error)?.toString()}")
            }

            val nsData = NSData.dataWithBytes(CFDataGetBytePtr(decryptedData), CFDataGetLength(decryptedData).toULong())
            return NSString.create(data = nsData, encoding = NSUTF8StringEncoding) as String? ?: throw Exception("Failed to decode decrypted data")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun signWithPrivateKey(privateKeyBase64: String, message: String): String {
        val privateKey =
            loadPrivateKey(privateKeyBase64) ?: throw Exception("Invalid private key")
        val messageData = message.encodeToByteArray()

        memScoped {
            val errorPtr = alloc<CFErrorRefVar>()
            val cfData = messageData.toCFData()
            val signature = SecKeyCreateSignature(
                privateKey,
                kSecKeyAlgorithmRSASignatureMessagePKCS1v15SHA256,
                cfData,
                errorPtr.ptr
            )

            if (signature == null) {
                val error = errorPtr.value
                throw Exception("Signing failed: ${CFErrorCopyDescription(error)?.toString()}")
            }

            val nsData = NSData.dataWithBytes(
                CFDataGetBytePtr(signature),
                CFDataGetLength(signature).toULong()
            )
            return nsData.base64EncodedStringWithOptions(0u)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun verifyWithPublicKey(publicKeyBase64: String, message: String, signature: String): Boolean {
        val publicKey = loadPublicKey(publicKeyBase64) ?: throw Exception("Invalid public key")
        val messageData = message.encodeToByteArray()

        val signatureData = NSData.create(base64EncodedString = signature, options = 1U /* NSDataBase64DecodingOptions.IgnoreUnknownCharacters */)
            ?: throw Exception("Invalid base64 encoded signature")


        memScoped {
            val errorPtr = alloc<CFErrorRefVar>()
            val cfData = messageData.toCFData()
            val cfSignatureData = signatureData as CFDataRef
            return SecKeyVerifySignature(
                publicKey,
                kSecKeyAlgorithmRSASignatureMessagePKCS1v15SHA256,
                cfData,
                cfSignatureData,
                errorPtr.ptr
            )
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun generateKeyPair(): UserKeys {
        memScoped {
            val tag = "com.yourdomain.app.keypair".encodeToByteArray()
            val attributes = mapOf(
                kSecAttrKeyType to kSecAttrKeyTypeRSA,
                kSecAttrKeySizeInBits to 2048,
                kSecPrivateKeyAttrs to mapOf(
                    kSecAttrIsPermanent to false,
                    kSecAttrApplicationTag to tag
                )
            )

            val errorPtr = alloc<CFErrorRefVar>()
            val privateKey = SecKeyCreateRandomKey(attributes as CFDictionaryRef, errorPtr.ptr)
                ?: throw Exception("Key generation failed: ${CFErrorCopyDescription(errorPtr.value)?.toString()}")

            val publicKey = SecKeyCopyPublicKey(privateKey)
                ?: throw Exception("Failed to extract public key")

            val privateKeyData = SecKeyCopyExternalRepresentation(privateKey, errorPtr.ptr)
                ?: throw Exception(
                    "Failed to export private key: ${
                        CFErrorCopyDescription(
                            errorPtr.value
                        )?.toString()
                    }"
                )

            val publicKeyData = SecKeyCopyExternalRepresentation(publicKey, errorPtr.ptr)
                ?: throw Exception(
                    "Failed to export public key: ${
                        CFErrorCopyDescription(
                            errorPtr.value
                        )?.toString()
                    }"
                )

            val privateKeyNSData = NSData.dataWithBytes(
                CFDataGetBytePtr(privateKeyData),
                CFDataGetLength(privateKeyData).toULong()
            )
            val publicKeyNSData = NSData.dataWithBytes(
                CFDataGetBytePtr(publicKeyData),
                CFDataGetLength(publicKeyData).toULong()
            )

            val privateKeyBase64 = privateKeyNSData.base64EncodedStringWithOptions(0u)
            val publicKeyBase64 = publicKeyNSData.base64EncodedStringWithOptions(0u)

            return UserKeys(publicKeyBase64, privateKeyBase64)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun validatePublicKey(publicKeyBase64: String): Boolean {
        return loadPublicKey(publicKeyBase64) != null
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun validatePrivateKey(privateKeyBase64: String): Boolean {
        return loadPrivateKey(privateKeyBase64) != null
    }

    actual suspend fun signChallenge(challenge: String, privateKeyBase64: String): String {
        return signWithPrivateKey(privateKeyBase64, challenge)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun loadPublicKey(base64Encoded: String): SecKeyRef? {
        val keyData = NSData.create(base64EncodedString = base64Encoded, options = 1U /* Corresponds to NSDataBase64DecodingOptions.IgnoreUnknownCharacters */)
            ?: return null
        val attributes = mapOf(
            kSecAttrKeyType to kSecAttrKeyTypeRSA,
            kSecAttrKeyClass to kSecAttrKeyClassPublic,
            kSecAttrKeySizeInBits to 2048,
            kSecReturnPersistentRef to true
        )

        memScoped {
            val errorPtr = alloc<CFErrorRefVar>()
            return SecKeyCreateWithData(
                keyData as CFDataRef,
                attributes as CFDictionaryRef,
                errorPtr.ptr
            )
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun loadPrivateKey(base64Encoded: String): SecKeyRef? {
        val keyData = NSData.create(base64EncodedString = base64Encoded, options = 1U /* Corresponds to NSDataBase64DecodingOptions.IgnoreUnknownCharacters */)
            ?: return null
        val attributes = mapOf(
            kSecAttrKeyType to kSecAttrKeyTypeRSA,
            kSecAttrKeyClass to kSecAttrKeyClassPrivate,
            kSecAttrKeySizeInBits to 2048,
            kSecReturnPersistentRef to true
        )

        memScoped {
            val errorPtr = alloc<CFErrorRefVar>()
            return SecKeyCreateWithData(
                keyData as CFDataRef,
                attributes as CFDictionaryRef,
                errorPtr.ptr
            )
        }
    }

}

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toCFData(): CFDataRef {
    val uByteArray = this.asUByteArray()
    return uByteArray.usePinned { pinnedData ->
        CFDataCreate(null, pinnedData.addressOf(0), this.size.toLong())
            ?: throw Exception("Failed to create CFData")
    }
}
