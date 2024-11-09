package cn.llonvne

import cn.llonvne.type.ApiImplement
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.jboss.logging.Logger

class ApiImplementResolver(
) {
    private val logger: Logger = Logger.getLogger(ApiImplementResolver::class.java, "api")
    fun resolve(api: ApiImplement): HttpHandler {

        logger.info(
            "${api.name().generateName(api)} service established at uri:${
                api.uri().getUri(api)
            }."
        )

        return {
            Response(Status.OK).body(api.toString())
        }
    }

    fun manageHandler(): HttpHandler {
        TODO()
    }
}