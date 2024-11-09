package cn.llonvne.type

import kotlin.reflect.KClass

interface ApiImplement {
    fun apiCls(): KClass<*>

    fun name(): ApiNamePolicy = ApiNamePolicy.AutoGenerated

    fun uri(): UriPolicy = UriPolicy.AutoGenerated
}