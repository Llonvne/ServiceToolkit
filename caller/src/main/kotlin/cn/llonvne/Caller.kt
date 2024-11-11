package cn.llonvne

import cn.llonvne.type.ApplicationMeta
import cn.llonvne.type.ServiceResponse
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Uri
import org.http4k.core.appendToPath
import org.http4k.format.Jackson.auto
import kotlin.reflect.KClass


abstract class Caller<T>(
    host: String,
    port: Int,
    cls: KClass<*>,
    okHttpClient: OkHttpClient = OkHttpClient(),
) {
    private val handler = OkHttp(okHttpClient)

    private val baseUri = Uri.of("http://$host:$port")

    val meta = Body.auto<ServiceResponse<ApplicationMeta>>().toLens()(
        handler(
            org.http4k.core.Request(
                Method.POST,
                baseUri.appendToPath("meta")
            )
        )
    ).value.map[cls.qualifiedName.toString()]
        ?: throw RuntimeException("meta of ${cls.qualifiedName} not found on target server")

    val instance: T by lazy {

        val proxyClsFqname = "cn.llonvne." + cls.simpleName.toString() + "Proxy"
        val kclass = Class.forName(proxyClsFqname).kotlin

        kclass.constructors.first().call(
            handler,
            object : ApiMeta {
                override fun uri(name: String): String {
                    return baseUri
                        .appendToPath(meta.uri)
                        .appendToPath(name).toString()
                }
            }
        ) as T
    }
}

