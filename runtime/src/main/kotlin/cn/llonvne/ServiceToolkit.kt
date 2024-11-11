package cn.llonvne

import cn.llonvne.type.ApiHost
import cn.llonvne.type.ApiImplement
import cn.llonvne.type.ApplicationMeta
import cn.llonvne.type.ServiceMeta
import cn.llonvne.type.ServiceResponse
import cn.llonvne.type.ServiceToolkitApi
import org.http4k.core.Body
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer

fun getApiHostMap(apiImplement: List<ApiImplement>): Map<KClass<*>, ApiHost> {
    val cls: KClass<*> = Class.forName("cn.llonvne.GeneratedHandlers").kotlin
    val generatedHandlers = cls.constructors.first().call()
    val method = cls.memberFunctions.first { it.name == "getApiHost" }
    val apiHosts: List<ApiHost> =
        method.call(generatedHandlers, apiImplement).also { println(it) } as List<ApiHost>
    return apiHosts.associateBy { it.apiCls() }
}

private class ServiceToolkitApplication(
    port: Int,
    apiImplement: List<ApiImplement>,
) : ServiceToolkitApi {

    private val hostMap = getApiHostMap(apiImplement)

    private val apiImplementResolver = ApiImplementResolver()

    private val applicationMeta = ApplicationMeta(mutableMapOf())

    private val handlers =
        apiImplement
            .map { it to apiImplementResolver.resolve(it, hostMap[it.apiCls()]!!) }
            .map { (api, handler) ->
                applicationMeta.map[api.apiCls().qualifiedName.toString()] = ServiceMeta(
                    api.uri().getUri(api),
                    api.apiCls().qualifiedName.toString()
                )
                api.uri().getUri(api) bind Method.POST to handler
            }
            .toList()

    val metaHandler = "/meta" bind Method.POST to {
        Response(Status.OK).with(
            Body.auto<ServiceResponse<ApplicationMeta>>().toLens() of ServiceResponse(applicationMeta)
        )
    }

    private val server = routes(handlers.toMutableList().apply { add(metaHandler) }).asServer(Undertow(port))
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

fun runServiceToolkit(port: Int, vararg implement: ApiImplement): ServiceToolkitApi =
    ServiceToolkitApplication(port, implement.toList())
