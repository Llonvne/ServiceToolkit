package cn.llonvne.type

import org.http4k.core.HttpHandler

interface ApiHost {
    fun getHandler(): HttpHandler
}