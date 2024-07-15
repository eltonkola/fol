package com.fol.com.fol.db.model

import com.fol.com.fol.model.Message
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.datetime.Instant

class AppMessage : RealmObject , Author {
    @PrimaryKey
    var id: String = RealmUUID.random().toString()
    var message: String = ""
    var senderKey: String = ""
    var receiverKey: String = ""
    var sent: Boolean = false
    var received: Boolean = false
    var timeSent: RealmInstant = RealmInstant.now()
    private var typeString: String = MessageType.TEXT.state

    var type: MessageType
        get() = MessageType.entries.first { it.state == typeString }
        set(value) {
            typeString = value.state
        }

}

//TODO - support more message types, for now only text will be there.
enum class MessageType(val state: String) {
    TEXT("text"),
    IMAGE("image"),
    VIDEO("video");
}

fun RealmInstant.toInstant(): Instant {
    return Instant.fromEpochSeconds(this.epochSeconds, this.nanosecondsOfSecond.toLong())
}

fun AppMessage.normalize(author: (String) -> Author) : Message {
    return Message(
        message = this.message,
        kur = this.timeSent.toInstant(),
        author = author(this.senderKey)
    )
}