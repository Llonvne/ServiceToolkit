package cn.llonvne

import kotlin.reflect.KClass

/**
 * 该注解将指示被注解类型将生成远程远程调用代理类
 *
 * 请注意生成的远程调用代理类必须在api参数中指定,并且被注解类型必须实现api指定的类型，和[cn.llonvne.type.ApiImplement]类型。
 */
annotation class ServiceImpl(
    val api: KClass<*>
)