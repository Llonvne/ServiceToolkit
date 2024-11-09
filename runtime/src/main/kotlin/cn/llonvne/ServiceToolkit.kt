package cn.llonvne

import cn.llonvne.type.ApiImplement
import cn.llonvne.type.ServiceToolkitApi
import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer


private class ServiceToolkitApplication(
    val port: Int,
    val apiImplement: List<ApiImplement>,
) : ServiceToolkitApi {

    private val apiImplementResolver = ApiImplementResolver()

    val handlers = apiImplement.map {
        it to apiImplementResolver.resolve(it)
    }.map { (api, handler) ->
        api.uri().getUri(api) bind Method.POST to handler
    }.toList()

    private val app = routes(
        handlers
    )
    private val server = app.asServer(Undertow(port))
    override fun start() {
        server.start()
    }

    override fun stop() {
        server.stop()
    }

    override fun block() {
        server.block()
    }
}

fun runServiceToolkit(
    port: Int,
    vararg implement: ApiImplement
): ServiceToolkitApi = ServiceToolkitApplication(port, implement.toList())