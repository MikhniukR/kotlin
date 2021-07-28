plugins {
    id("org.jetbrains.kotlin.multiplatform").version("<pluginMarkerVersion>")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    <SingleNativeTarget>("native") {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
        sourceSets["commonMain"].dependencies {
            implementation("io.ktor:ktor-client-core:1.5.4")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC-native-mt")
        }
    }
}

println("for_test_kotlin_native_target=<SingleNativeTarget>")
