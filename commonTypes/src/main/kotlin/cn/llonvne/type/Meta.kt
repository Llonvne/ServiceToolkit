package cn.llonvne.type

class ApplicationMeta(
    val map: MutableMap<String, ServiceMeta>
) {
}

class ServiceMeta(
    val uri: String,
    val serviceClsName: String
)