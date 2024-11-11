package cn.llonvne

import cn.llonvne.type.ApplicationMeta
import cn.llonvne.type.ServiceMeta
import cn.llonvne.type.ServiceResponse
import java.util.logging.Logger
import kotlin.reflect.KClass
import okhttp3.OkHttpClient
import org.http4k.client.DualSyncAsyncHttpHandler
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.core.appendToPath
import org.http4k.format.Jackson.auto

private class ApplicationMetaProvider(
    private val baseUri: Uri,
    private val handler: DualSyncAsyncHttpHandler,
    private val cls: KClass<*>,
    private val logger: Logger = Logger.getLogger("[CallerMeta-${cls.qualifiedName}]")
) {
    private val metaRequest = Request(Method.POST, baseUri.appendToPath("meta"))

    init {
        logger.info("meta request is $metaRequest.")
    }

    private var internalMeta = fetchMeta()

    init {
        logger.info("meta fetched successful.")
        logger.info("meta is $internalMeta")
    }

    private fun fetchMeta(): ApiMeta {
        return ApiMetaImpl(
            baseUri,
            Body.auto<ServiceResponse<ApplicationMeta>>().toLens()(handler(metaRequest))
                .value
                .map[cls.qualifiedName.toString()]
                ?: throw RuntimeException(
                    "meta of ${cls.qualifiedName} not found on target server"
                )
        )
    }

    fun refreshMeta() {
        val newMeta = fetchMeta()
        if (newMeta != internalMeta) {
            logger.warning("meta refreshed.")
            internalMeta = newMeta
        }
    }

    private data class ApiMetaImpl(val baseUri: Uri, val serviceMeta: ServiceMeta) : ApiMeta {
        override fun uri(name: String): String {
            return baseUri.appendToPath(serviceMeta.uri).appendToPath(name).toString()
        }

        override fun toString(): String {
            return "${serviceMeta.serviceClsName.split(".").last()} at ${serviceMeta.uri}"
        }
    }

    fun getMeta(): ApiMeta {
        return internalMeta
    }
}

abstract class Caller<T>(
    host: String,
    port: Int,
    private val cls: KClass<*>,
    okHttpClient: OkHttpClient = OkHttpClient(),
) {

    private val logger = Logger.getLogger("[Caller-${cls.qualifiedName}]")


    init {
        logger.info("caller-${cls.simpleName} initializing...")
    }

    private val handler = OkHttp(okHttpClient)

    private val baseUri = Uri.of("http://$host:$port")

    init {
        logger.info("baseUri is $baseUri.")
    }

    private val metaProvider = ApplicationMetaProvider(baseUri, handler, cls)

    private val getter = instanceGetter()

    val instance: T
        get() {
            return getter(metaProvider.getMeta())
        }

    fun refreshMeta() {
        metaProvider.refreshMeta()
    }

    private fun instanceGetter(): (ApiMeta) -> T {
        val proxyClsFqname = "cn.llonvne." + cls.simpleName.toString() + "Proxy"
        val kClass = try {
            Class.forName(proxyClsFqname).kotlin
        } catch (e: Exception) {
            logger.severe {
                "cannot find the $proxyClsFqname,ensure the ksp works."
            }
            throw e
        }
        val constructor = kClass.constructors.first()
        return { constructor.call(handler, it) as T }
    }
}
