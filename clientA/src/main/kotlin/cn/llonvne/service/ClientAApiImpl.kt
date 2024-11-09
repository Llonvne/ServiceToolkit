package cn.llonvne.service

import cn.llonvne.ServiceImpl
import cn.llonvne.api.ClientAApi
import cn.llonvne.runServiceToolkit
import cn.llonvne.type.ApiImplement
import cn.llonvne.type.ApiNamePolicy
import cn.llonvne.type.Message
import cn.llonvne.type.UriPolicy
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

    override fun apiCls(): KClass<*> {
        return ClientAApi::class
    }

    override fun name(): ApiNamePolicy {
        return ApiNamePolicy.Specified("<client-a api>")
    }

    override fun uri(): UriPolicy {
        return UriPolicy.Specified("a")
    }
}

fun main() {
    runServiceToolkit(
        8080,
        ClientAApiImpl()
    ).start()
}