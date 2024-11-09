package cn.llonvne.type

import org.http4k.core.HttpHandler
import kotlin.reflect.KClass

interface ApiHost {
    fun getHandler(): HttpHandler

    fun apiCls(): KClass<*>
}