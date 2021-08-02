plugins {
    kotlin("jvm")
}

dependencies {
//    implementation(kotlinStdlib())
    implementation(project(":compiler:psi"))
    implementation(project(":compiler:cli"))
    implementation(project(":idea-frontend-api"))
    implementation(project(":idea-frontend-fir"))
    implementation(intellijCoreDep())
    implementation(intellijCoreDep()) { includeJars("intellij-core") }
    implementation(intellijDep()) { includeIntellijCoreJarDependencies(project) }
    implementation(intellijDep()) { includeJars("intellij-deps-fastutil-8.4.1-4") }
    implementation(jpsStandalone()) { includeJars("jps-model") }

    //try to fix run
    implementation(project(":kotlin-reflect"))

    //copy from idea-frontend-fir

    implementation(project(":compiler:psi"))
    implementation(project(":compiler:fir:fir2ir"))
    implementation(project(":compiler:ir.tree"))
    implementation(project(":compiler:fir:resolve"))
    implementation(project(":compiler:fir:checkers"))
    implementation(project(":compiler:fir:checkers:checkers.jvm"))
    implementation(project(":compiler:fir:java"))
    implementation(project(":compiler:fir:jvm"))
    implementation(project(":idea-frontend-fir:idea-fir-low-level-api"))
    implementation(project(":idea-frontend-api"))
    implementation(project(":compiler:light-classes"))
    implementation(projectTests(":idea-frontend-fir:idea-fir-low-level-api"))

    //copy from compiler:fir:analysis-tests
    implementation(intellijCoreDep()) { includeJars("intellij-core", "guava", rootProject = rootProject) }

    implementation(project(":compiler:cli"))
    implementation(project(":compiler:fir:checkers"))
    implementation(project(":compiler:fir:checkers:checkers.jvm"))
    implementation(project(":compiler:fir:fir-serialization"))
    implementation(project(":compiler:fir:entrypoint"))
    implementation(project(":compiler:frontend"))

    implementation(project(":kotlin-reflect-api"))
    implementation(project(":kotlin-reflect"))
    implementation(project(":core:descriptors.runtime"))
    implementation(project(":compiler:fir:fir2ir:jvm-backend"))

    implementation(intellijCoreDep()) { includeJars("intellij-core") }
    implementation(intellijDep()) {
        includeJars("jna", rootProject = rootProject)
    }

//    implementation(intellijDep()) { includeJars("intellij-deps-fastutil-8.4.1-4") }
    implementation(toolsJar())

    //
    implementation(files("/Users/Roman.Mikhniuk/work/kotlin/libraries/stdlib/jvm/build/libs/kotlin-stdlib-1.6.255-SNAPSHOT.jar"))
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
