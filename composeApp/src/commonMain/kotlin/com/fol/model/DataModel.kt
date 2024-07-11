package com.fol.com.fol.model

import kotlinx.datetime.Instant


data class Contact(
    val name : String,
    val publicKey : String
)

data class Thread(
    val contact: Contact,
    val messages : List<Message>,
)

data class ThreadPreview(
    val id : String,
    val contact: Contact,
    val lastMessage : Message,
)

data class Message(
    val message : String,
    val kur : Instant
)
