package cn.llonvne

import cn.llonvne.api.ClientAApi

@GenerateCallerProxy(ClientAApi::class)
class ClientAApiCaller : Caller<ClientAApi>(
    "localhost", 8080, ClientAApi::class
) {
}

fun main() {
    val caller = ClientAApiCaller().instance
    println(caller.ok())
}