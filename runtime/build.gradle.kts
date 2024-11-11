plugins {
    kotlin("jvm")
}

group = "cn.llonvne"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    api(platform("org.http4k:http4k-bom:5.33.0.1"))
    api(platform("org.http4k:http4k-connect-bom:5.24.1.0"))
    api("org.http4k:http4k-client-okhttp")
    api("org.http4k:http4k-core")
    api("org.http4k:http4k-format-jackson")
    api("org.http4k:http4k-server-undertow")
    implementation(projects.commonTypes)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}