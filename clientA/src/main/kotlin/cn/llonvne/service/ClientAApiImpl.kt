package cn.llonvne.service

import cn.llonvne.ServiceImpl
import cn.llonvne.api.ClientAApi
import cn.llonvne.runServiceToolkit
import cn.llonvne.type.ApiImplement
import cn.llonvne.type.ApiNamePolicy
import cn.llonvne.type.Message
import cn.llonvne.type.UriPolicy
import java.util.UUID
import kotlin.reflect.KClass

@ServiceImpl(ClientAApi::class)
class ClientAApiImpl : ClientAApi, ApiImplement {
    override fun send(message: Message): String {
        println(message)
        return message.body
    }

    override fun ok() {
        println("OK")
    }

    override fun login(username: String, password: String): Boolean {
        return username.length == password.length
    }

    override fun apiCls(): KClass<*> {
        return ClientAApi::class
    }

    override fun name(): ApiNamePolicy {
        return ApiNamePolicy.Specified("<client-a api>")
    }

    override fun uri(): UriPolicy {
        return UriPolicy.Specified("123456")
    }
}

fun main() {
    runServiceToolkit(
        8080,
        ClientAApiImpl()
    ).start()
}