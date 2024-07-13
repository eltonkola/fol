package com.fol.com.fol.db

import co.touchlab.kermit.Logger
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

        val config = RealmConfiguration.Builder(schema = setOf(AppContact::class, AppProfile::class))
            .encryptionKey(getEncryptionKey(passcode))
            .build()

        realm = Realm.open(config)
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

}
