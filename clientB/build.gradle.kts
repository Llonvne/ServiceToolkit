plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "cn.llonvne"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.caller)
    implementation(projects.clientAApi)
    ksp(projects.callKsp)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}