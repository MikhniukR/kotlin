/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.implementations

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import org.jetbrains.kotlin.analyzer.LibraryModuleInfo
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.analyzer.ModuleSourceInfoBase
import org.jetbrains.kotlin.analyzer.common.CommonPlatformAnalyzerServices
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleCapability
import org.jetbrains.kotlin.fir.DependencyListForCliModule
import org.jetbrains.kotlin.fir.FirModuleDataImpl
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.SealedClassInheritorsProvider
import org.jetbrains.kotlin.fir.declarations.SealedClassInheritorsProviderImpl
import org.jetbrains.kotlin.fir.dependenciesWithoutSelf
import org.jetbrains.kotlin.fir.deserialization.ModuleDataProvider
import org.jetbrains.kotlin.fir.deserialization.SingleModuleDataProvider
import org.jetbrains.kotlin.fir.session.FirModuleInfoBasedModuleData
import org.jetbrains.kotlin.idea.fir.low.level.api.api.FirModuleResolveStateConfigurator
import org.jetbrains.kotlin.load.kotlin.PackagePartProvider
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms.unspecifiedJvmPlatform
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import java.nio.file.Path
import java.nio.file.Paths

class FirModuleResolveStateConfiguratorImpl(
    private val project: Project,
    private val languageVersionSettings: LanguageVersionSettings,
    private val ktFiles: List<KtFile>
) : FirModuleResolveStateConfigurator() {
    override fun createPackagePartsProvider(moduleInfo: ModuleSourceInfoBase, scope: GlobalSearchScope): PackagePartProvider =
        PackagePartProvider.Empty

    override fun createModuleDataProvider(moduleInfo: ModuleSourceInfoBase): ModuleDataProvider {
        return DependencyListForCliModule.build(
            moduleInfo.name,
            moduleInfo.platform,
            moduleInfo.analyzerServices
        ) {
            dependencies(moduleInfo.dependenciesWithoutSelf().extractLibraryPaths(project))
            friendDependencies(moduleInfo.modulesWhoseInternalsAreVisible().extractLibraryPaths(project))
            dependsOnDependencies(moduleInfo.expectedBy.extractLibraryPaths(project))

            val moduleData = FirModuleInfoBasedModuleData(moduleInfo)
            sourceDependencies(moduleData.dependencies)
            sourceFriendsDependencies(moduleData.friendDependencies)
            sourceDependsOnDependencies(moduleData.dependsOnDependencies)
        }.moduleDataProvider
//        return SingleModuleDataProvider(
//            FirModuleDataImpl(
//                moduleInfo.name,
//                emptyList(),
//                emptyList(),
//                emptyList(),
//                moduleInfo.platform,
//                moduleInfo.analyzerServices
//            )
//        )
//        return SingleModuleDataProvider(FirModuleInfoBasedModuleData(moduleInfo))
    }

    override fun getLanguageVersionSettings(moduleInfo: ModuleSourceInfoBase): LanguageVersionSettings = languageVersionSettings

    override fun getModuleSourceScope(moduleInfo: ModuleSourceInfoBase): GlobalSearchScope =
        TopDownAnalyzerFacadeForJVM.newModuleSearchScope(project, ktFiles)

    override fun createScopeForModuleLibraries(moduleInfo: ModuleSourceInfoBase): GlobalSearchScope =
        ProjectScope.getLibrariesScope(project)

    override fun createSealedInheritorsProvider(): SealedClassInheritorsProvider {
        return SealedClassInheritorsProviderImpl
    }

    override fun getModuleInfoFor(element: KtElement): ModuleInfo = ModuleInfoImpl()

    private fun Sequence<ModuleInfo>.extractLibraryPaths(project: Project): List<Path> {
        return fold(mutableListOf()) { acc, moduleInfo ->
            moduleInfo.extractLibraryPaths(acc)
            acc
        }
    }

    private fun Iterable<ModuleInfo>.extractLibraryPaths(project: Project): List<Path> {
        return fold(mutableListOf()) { acc, moduleInfo ->
            moduleInfo.extractLibraryPaths(acc)
            acc
        }
    }

    private fun ModuleInfo.extractLibraryPaths(destination: MutableList<Path>) {
        when (this) {
//            is SdkInfoBase -> {
//                val sdk = (this as SdkInfo).sdk
//                sdk.rootProvider.getFiles(OrderRootType.CLASSES).mapNotNullTo(destination) {
//                    Paths.get(it.fileSystem.extractPresentableUrl(it.path)).normalize()
//                }
//            }
            is LibraryModuleInfo -> {
                getLibraryRoots().mapTo(destination) {
                    Paths.get(it).normalize()
                }
            }
        }
    }


    override fun configureSourceSession(session: FirSession) {
    }
}

internal class ModuleInfoImpl(
    override val name: Name = Name.identifier("Name"),
    override val capabilities: Map<ModuleCapability<*>, Any?> = emptyMap(),
    override val expectedBy: List<ModuleInfo> = emptyList(),
    override val platform: TargetPlatform = TargetPlatform(
        setOf(
            unspecifiedJvmPlatform.single()
//            defaultJsPlatform.single(),
//            unspecifiedNativePlatform.single()
        )
    )
) : ModuleSourceInfoBase {
    override fun dependencies() = listOf(this, StdLibraryModuleInfo(), JdkModuleInfo())

    override fun modulesWhoseInternalsAreVisible(): Collection<ModuleInfo> = emptyList()

    override val analyzerServices: PlatformDependentAnalyzerServices
        get() = CommonPlatformAnalyzerServices
}

internal class StdLibraryModuleInfo(
    override val name: Name = Name.identifier("stdlib"),
    override val platform: TargetPlatform = TargetPlatform(
        setOf(
            unspecifiedJvmPlatform.single()
        )
    )
) : LibraryModuleInfo, ModuleSourceInfoBase {
    override val analyzerServices: PlatformDependentAnalyzerServices = CommonPlatformAnalyzerServices

    override fun getLibraryRoots(): Collection<String> =
        listOf(
            "/Users/Roman.Mikhniuk/work/kotlin/libraries/stdlib/jvm/build/libs/kotlin-stdlib-1.6.255-SNAPSHOT.jar",
            "/Users/Roman.Mikhniuk/Library/Application Support/JetBrains/Toolbox/apps/IDEA-U/ch-0/212.4746.92/IntelliJ IDEA.app/Contents/plugins/Kotlin/kotlinc/lib/kotlin-stdlib.jar",
            "/Users/Roman.Mikhniuk/work/kotlin/core/builtins/build/libs/builtins-1.6.255-SNAPSHOT.jar",
            "/Users/Roman.Mikhniuk/work/kotlin/libraries/scripting/jvm-host-embeddable/build/libs/kotlin-scripting-jvm-host-1.6.255-SNAPSHOT.jar",
            "/Users/Roman.Mikhniuk/.gradle/wrapper/dists/gradle-6.9-bin/2ecsmyp3bolyybemj56vfn4mt/gradle-6.9/lib/kotlin-stdlib-1.4.20.jar",
            "/Users/Roman.Mikhniuk/.gradle/wrapper/dists/gradle-6.9-bin/2ecsmyp3bolyybemj56vfn4mt/gradle-6.9/lib/kotlin-stdlib-common-1.4.20.jar",
            "/Users/Roman.Mikhniuk/.gradle/wrapper/dists/gradle-6.9-bin/2ecsmyp3bolyybemj56vfn4mt/gradle-6.9/lib/kotlin-stdlib-jdk7-1.4.20.jar",
            "/Users/Roman.Mikhniuk/.gradle/wrapper/dists/gradle-6.9-bin/2ecsmyp3bolyybemj56vfn4mt/gradle-6.9/lib/kotlin-stdlib-jdk8-1.4.20.jar"
        )

    override fun dependencies(): List<ModuleInfo> = listOf(this, JdkModuleInfo())

}

internal class JdkModuleInfo(
    override val name: Name = Name.identifier("jdk"),
    override val platform: TargetPlatform = TargetPlatform(
        setOf(
            unspecifiedJvmPlatform.single()
        )
    )
) : LibraryModuleInfo, ModuleSourceInfoBase {
    override val analyzerServices: PlatformDependentAnalyzerServices = CommonPlatformAnalyzerServices

    override fun getLibraryRoots(): Collection<String> =
        listOf(
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/charsets.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/ext/cldrdata.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/ext/dnsns.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/ext/jaccess.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/ext/localedata.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/ext/nashorn.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/ext/sunec.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/ext/sunjce_provider.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/ext/sunpkcs11.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/ext/zipfs.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/jce.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/jfr.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/jsse.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/management-agent.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/rt.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/lib/dt.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/lib/jconsole.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/lib/sa-jdi.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/lib/tools.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/lib/dt.jar",
            "/Users/Roman.Mikhniuk/.gradle/jdks/jdk8u292-b10/Contents/Home/jre/lib/resources.jar",
        )

    override fun dependencies(): List<ModuleInfo> = listOf(this)

}

//LibraryModuleInfo, прописать все пути к jar в getLibraryRoots
/*
std lib состоит из нескольких jar-ников, нужно будет все закинуть
возможно придется добавить moduleInfo на JDK и зависить на неё из обоих модулей
 */