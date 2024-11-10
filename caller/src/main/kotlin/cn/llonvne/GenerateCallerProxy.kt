package cn.llonvne

import kotlin.reflect.KClass
@Repeatable
annotation class GenerateCallerProxy(
    val cls: KClass<*>
)