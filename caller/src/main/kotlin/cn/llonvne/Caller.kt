package cn.llonvne

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.http4k.client.OkHttp
import org.http4k.core.Uri
import org.http4k.core.appendToPath
import kotlin.reflect.KClass

class BasePathInterceptor(private val basePath: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val url = basePath.toHttpUrlOrNull()?.newBuilder()
            ?.addPathSegments(request.url.encodedPath.trimStart('/'))
            ?.build()

        if (url != null) {
            request = request.newBuilder()
                .url(url)
                .build()
        }
        return chain.proceed(request)
    }
}

abstract class Caller<T>(
    host: String,
    port: Int,
    cls: KClass<*>,
    okHttpClient: OkHttpClient = OkHttpClient(),
) {
    private val handler = OkHttp(okHttpClient)

    private val baseUri = Uri.of("http://$host:$port").appendToPath("a")

    val instance: T by lazy {

        val proxyClsFqname = "cn.llonvne." + cls.simpleName.toString() + "Proxy"
        val kclass = Class.forName(proxyClsFqname).kotlin

        kclass.constructors.first().call(
            handler,
            object : ApiMeta {
                override fun uri(name: String): String {
                    return baseUri.appendToPath(name).toString()
                }
            }
        ) as T
    }
}

