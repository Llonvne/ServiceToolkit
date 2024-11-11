package cn.llonvne.type

data class ApplicationMeta(
    val map: MutableMap<String, ServiceMeta>
) {
}

data class ServiceMeta(
    val uri: String,
    val serviceClsName: String
)