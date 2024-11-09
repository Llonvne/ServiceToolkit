plugins {
    kotlin("jvm")
}

group = "cn.llonvne"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(projects.commonApiAnno)
    api(projects.commonTypes)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}