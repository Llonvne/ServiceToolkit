package cn.llonvne

import cn.llonvne.type.ApiHost
import cn.llonvne.type.ApiImplement
import cn.llonvne.type.ServiceToolkitApi
import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions


private class ServiceToolkitApplication(
    val port: Int,
    val apiImplement: List<ApiImplement>,
) : ServiceToolkitApi {


    val cls: KClass<*> = Class.forName("cn.llonvne.GeneratedHandlers").kotlin
    val generatedHandlers = cls.constructors.first().call()
    val method = cls.memberFunctions.first {
        it.name == "getApiHost"
    }
    val apiHosts: List<ApiHost> = method.call(generatedHandlers, apiImplement)
        .also { println(it) } as List<ApiHost>
    val handlerMap = apiHosts.associateBy { it.apiCls() }

    private val apiImplementResolver = ApiImplementResolver()

    val handlers = apiImplement.map {
        it to apiImplementResolver.resolve(it, handlerMap[it.apiCls()]!!)
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