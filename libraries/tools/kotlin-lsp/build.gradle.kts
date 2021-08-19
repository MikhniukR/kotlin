import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow")
}


dependencies {
    implementation(kotlinStdlib())
    implementation(project(":compiler:psi"))
    implementation(project(":compiler:cli"))
    implementation(project(":idea-frontend-api"))
    implementation(project(":idea-frontend-fir"))
    implementation(project(":compiler:fir:entrypoint"))
    implementation(project(":compiler:fir:cones"))
    implementation(intellijCoreDep())
    implementation(intellijCoreDep()) { includeJars("intellij-core") }
    implementation(intellijDep()) { includeIntellijCoreJarDependencies(project) }
    implementation(intellijDep()) { includeJars("intellij-deps-fastutil-8.4.1-4") }
    implementation(jpsStandalone()) { includeJars("jps-model") }

    //logging
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")

    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.12.0")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.12.0")

    testApiJUnit5()
}

sourceSets {
    "main" {
        projectDefault()
    }
    "test" {
        projectDefault()
    }
}

projectTest(jUnit5Enabled = true) {
    dependsOn(":dist")
    workingDir = rootDir

    useJUnitPlatform()
}

// todo fix later
tasks.withType<ProcessResources> {
//    from(zipTree("/Users/Roman.Mikhniuk/work/kotlin/libraries/stdlib/jvm/build/libs/kotlin-stdlib-1.6.255-SNAPSHOT.jar"))
    from(zipTree("/Users/Roman.Mikhniuk/tmp/kotlin-stdlib-1.6.255-SNAPSHOT.jar"))
}
//todo add to verification-metadata
disableDependencyVerification()

application {
    mainClassName = "org.jetbrains.kotlin.lsp.MainKt"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("kotlin-lsp")
        mergeServiceFiles()
    }
}