package com.fol.com.fol.db

import co.touchlab.kermit.Logger
import com.fol.com.fol.db.model.AppContact
import com.fol.com.fol.db.model.AppMessage
import com.fol.com.fol.db.model.AppProfile
import com.fol.com.fol.model.Message
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map



class DbManager {

    private lateinit var realm: Realm

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun getEncryptionKey(pin: String): ByteArray {
        require(pin.length == 6) { "PIN must be 6 digits" }

        // Hash the PIN
        val hash = pin.encodeToUtf8ByteArray().sha256()

        // Repeat the hash to get 64 bytes
        return (hash + hash).toUByteArray().asByteArray()
    }

    private fun openDatabase(passcode: String) {


        val config = RealmConfiguration.Builder(
            schema = setOf(
                AppContact::class,
                AppProfile::class,
                AppMessage::class
            )
        )
            .encryptionKey(getEncryptionKey(passcode))
            .build()

        realm = Realm.open(config)
       Logger.i { "openDatabase with passcode: $passcode - realm: $realm" }
    }

    fun op() {

        // Write
        realm.writeBlocking {
            copyToRealm(AppContact().apply {
                name = "John Doe"
                publicKey = "asdasdasd"
            })
        }

        // Read
        val allContacts = realm.query<AppContact>().find()

        // Update
        realm.writeBlocking {
            query<AppContact>("name == $0", "John Doe")
                .first().find()
                ?.id = "1"
        }

        // Delete
        realm.writeBlocking {
            query<AppContact>("name == $0", "John Doe")
                .first().find()
                ?.let { delete(it) }
        }
    }


    fun createProfile(passcode: String, privateKey: String, publicKey: String) : AppProfile? {
        openDatabase(passcode)
        realm.writeBlocking {
            val profile = query<AppProfile>().first().find() ?: AppProfile()
            copyToRealm(profile.apply {
                this.privateKey = privateKey
                this.publicKey = publicKey
            })
        }
        return getProfile()
    }

    fun loadProfile(passcode: String) : AppProfile? {
        openDatabase(passcode)
        return getProfile()
    }

    private fun getProfile(): AppProfile? {
        val profile = realm.query<AppProfile>().first().find()
        return profile
    }

    fun deleteAllData(){
        realm.writeBlocking {
            this.deleteAll()
        }
    }

    fun nuke(){
        Realm.deleteRealm(RealmConfiguration.Builder(
            schema = setOf(
                AppContact::class,
                AppProfile::class,
                AppMessage::class
            )
        ).build())
    }

    fun addContact(name: String, publicKey: String) {
        Logger.i { "addContact name: $name - realm: $realm" }
         realm.writeBlocking {
            val profile = query<AppContact>("publicKey == $0", publicKey).first().find() ?: AppContact()
            copyToRealm(profile.apply {
                this.name = name
                this.publicKey = publicKey
            })
        }
    }

    fun contacts(): Flow<List<AppContact>> {
        Logger.i { "contacts - realm: $realm" }
        return realm.query<AppContact>().asFlow(emptyList()).map { it.list }
    }

    fun getContactById(userId: String): AppContact? {
        Logger.i { "getContactById - userId: $userId" }
        return realm.query<AppContact>("id == $0", userId).first().find()
    }

    fun deleteContactById(id: String) {
        Logger.i { "deleteContactById - id: $id" }
        realm.writeBlocking {
            query<AppContact>("id == $0", id)
                .first().find()
                ?.let { delete(it) }
        }
    }

    fun addMessage(message: AppMessage) {
        realm.writeBlocking {
            copyToRealm(message)
        }
    }

    fun getMessages(receiverKey: String, senderKey: String): Flow<List<AppMessage>> {
        return realm.query<AppMessage>(
            "senderKey == $0 AND receiverKey == $1 OR senderKey == $1 AND receiverKey == $0",
            senderKey, receiverKey
        ).asFlow()
            .map { it.list }
    }

    //all messages we sent, but don't know if they are delivered to the other user
    fun getAllSentMessages(): List<AppMessage> {
        return realm.query<AppMessage>("sent == $0 AND received == $1 AND serverID > $2", true, false, 0).find()
    }

    fun messageConfirmedAsDelivered(publicID: Int) {
        Logger.i { "getMessageByPublicId - publicID: $publicID" }
        realm.writeBlocking {
            val message = realm.query<AppMessage>("serverID == $0", publicID).first().find()
            message?.let {
                it.received = true
                it.sent = true
            }
        }
    }

    fun unsentMessages(senderKey: String): Flow<List<AppMessage>> {
        Logger.i { "unsentMessages - realm: $realm" }
        return realm.query<AppMessage>(
            "sent == $0 AND senderKey == $1",
            false, senderKey
        ).asFlow().map { it.list }
    }

}
