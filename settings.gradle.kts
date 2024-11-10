plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include("commonApiAnno")
include("clientA")
include("clientB")
include("commonTypes")
include("clientAApi")
include("clientBApi")
include("serviceToolkitRuntime")
include("runtime")
include("apiKsp")
include("callKsp")
include("caller")
