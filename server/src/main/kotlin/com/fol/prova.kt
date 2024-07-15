package com.fol

import com.google.gson.Gson

fun main(){

    val request = WebSocketRequest(
        type = "test",
        data = mapOf("aa" to "aaa", "bb" to "cc")
    )

    val gson = Gson()
    val string = gson.toJson(request)
    println(string)

}