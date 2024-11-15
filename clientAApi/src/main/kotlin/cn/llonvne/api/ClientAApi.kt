package cn.llonvne.api

import cn.llonvne.type.Message

interface ClientAApi {
    fun send(message: Message): String

    fun ok()

    fun login(username: String, password: String): Boolean
}