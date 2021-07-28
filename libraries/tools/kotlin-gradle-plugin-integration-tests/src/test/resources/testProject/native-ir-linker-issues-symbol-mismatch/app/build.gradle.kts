plugins {
    id("org.jetbrains.kotlin.multiplatform").version("<pluginMarkerVersion>")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("<LocalRepo>")
}

kotlin {
    <SingleNativeTarget>("native") {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
        sourceSets["commonMain"].dependencies {
            implementation("org.sample:libb:1.0")
            implementation("org.sample:liba:2.0")
        }
    }
}

println("for_test_kotlin_native_target=<SingleNativeTarget>")
