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
    ksp(projects.apiKsp)
    implementation(projects.commonApiAnno)
    api(projects.clientAApi)
    implementation(projects.runtime)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}