package cn.llonvne

import cn.llonvne.api.ClientAApi
import cn.llonvne.api.MetaApi
import cn.llonvne.type.ApplicationMeta
import cn.llonvne.type.Message
import okhttp3.Call
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Request.Companion
import org.http4k.format.Jackson.auto
import java.time.Duration
import java.util.logging.Logger

@GenerateCallerProxy(ClientAApi::class)
class ClientAApiCaller : Caller<ClientAApi>(
    "localhost", 8080, ClientAApi::class
) {
}

fun main() {
    val caller = ClientAApiCaller()

    while (true) {
        try {
            Thread.sleep(Duration.ofSeconds(5).toMillis())
            caller.refreshMeta()
            caller.instance.send(Message("123", "!23"))
        } catch (e: Exception) {
            println(e.message)
        }
    }
}