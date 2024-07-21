package com.fol.crypto

import dev.whyoleg.cryptography.*
import dev.whyoleg.cryptography.algorithms.asymmetric.EC
import dev.whyoleg.cryptography.algorithms.asymmetric.ECDSA
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.symmetric.AES
import dev.whyoleg.cryptography.algorithms.symmetric.SymmetricKeySize
import dev.whyoleg.cryptography.operations.signature.*
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import dev.whyoleg.cryptography.algorithms.asymmetric.ECDH

fun main() = runBlocking{
    val appEncryption = AppEncryption()
    val pair = appEncryption.generateKeyPair()
    println("public key: ${pair.publicKey}")
    println("private key: ${pair.privateKey}")
    val text = "Ku je shqipe!"
    val encrypted = appEncryption.encryptWithPublicKey(pair.publicKey, text)
    println("encrypted: $encrypted")
    val decrypted = appEncryption.decryptWithPrivateKey(pair.privateKey, encrypted)
    println("decrypted: $decrypted")

    println("valid public key: ${appEncryption.validatePublicKey(pair.publicKey)}")
    println("invalid public key: ${appEncryption.validatePublicKey("kot")}")

    println("valid private key: ${appEncryption.validatePrivateKey(pair.privateKey)}")
    println("invalid private key: ${appEncryption.validatePrivateKey("kot")}")
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

    //1.	User Registration: Generate ECC and RSA key pairs for each user.
    //The user profile has a RSA key and a ECDSA key, one for encryption and the other for verification
    suspend fun createProfile() : UserKeys = withContext(Dispatchers.Default) {
        val ecdsaKeyGenerator = ecdsa.keyPairGenerator(EC.Curve.P521)
        val ecdsaKeyPair = ecdsaKeyGenerator.generateKey()
        val ecdsaPublicKey = ecdsaKeyPair.publicKey.encodeTo(EC.PublicKey.Format.DER).encodeBase64()
        val ecdsaPrivateKey = ecdsaKeyPair.privateKey.encodeTo(EC.PrivateKey.Format.DER).encodeBase64()

        val rsaKeyGenerator = rsa.keyPairGenerator()
        val rsaKeyPair = rsaKeyGenerator.generateKey()
        val rsaPublicKey = rsaKeyPair.publicKey.encodeTo(RSA.PublicKey.Format.DER).encodeBase64()
        val rsaPrivateKey = rsaKeyPair.privateKey.encodeTo(RSA.PrivateKey.Format.DER).encodeBase64()

        UserKeys(
            rsaPublicKey = rsaPublicKey,
            rsaPrivateKey = rsaPrivateKey,
            ecdsaPublicKey = ecdsaPublicKey,
            ecdsaPrivateKey = ecdsaPrivateKey,
        )
    }

    //	2.	Starting a Conversation: Generate an AES session key and encrypt it with the recipient’s RSA public key.
    suspend fun createSessionKey(receiverPublicKeyBase64: String, senderPrivateKeyBase64: String) : String = withContext(Dispatchers.Default) {
        val aesGcmKeyGenerator = aesGcm.keyGenerator(SymmetricKeySize.B256)
        val aesGcmKey : AES.GCM.Key = aesGcmKeyGenerator.generateKey()
        val aesGcmKeyValue = aesGcmKey.encodeToBlocking(AES.Key.Format.RAW).encodeBase64()

        encryptWithPublicKey(aesGcmKeyValue)

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
    val rsaPublicKey: String,
    val rsaPrivateKey: String,
    val ecdsaPublicKey: String,
    val ecdsaPrivateKey: String,
)
