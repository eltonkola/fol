package com.fol.com.fol.model

import com.fol.com.fol.db.AppContact
import kotlinx.datetime.Instant

data class Thread(
    val contact: AppContact,
    val messages : List<Message>,
)

data class ThreadPreview(
    val id : String,
    val contact: AppContact,
    val lastMessage : Message,
)

data class Message(
    val message : String,
    val kur : Instant
)
