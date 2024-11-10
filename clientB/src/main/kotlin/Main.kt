package cn.llonvne

import cn.llonvne.api.ClientAApi
import cn.llonvne.type.Message
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Request.Companion
import org.http4k.format.Jackson.auto

@GenerateCallerProxy(ClientAApi::class)
class ClientAApiCaller : Caller<ClientAApi>(
    "localhost", 8080, ClientAApi::class
) {
}

fun main() {
    val caller = ClientAApiCaller().instance
    println(caller.send(Message("Hello", "World")))
    OkHttp()(Request(POST, "http://localhost:8080/a/send"))
}