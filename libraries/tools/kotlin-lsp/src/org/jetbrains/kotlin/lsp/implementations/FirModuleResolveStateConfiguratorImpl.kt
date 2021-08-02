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
import org.jetbrains.kotlin.fir.FirModuleDataImpl
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.SealedClassInheritorsProvider
import org.jetbrains.kotlin.fir.declarations.SealedClassInheritorsProviderImpl
import org.jetbrains.kotlin.fir.deserialization.ModuleDataProvider
import org.jetbrains.kotlin.fir.deserialization.SingleModuleDataProvider
import org.jetbrains.kotlin.idea.fir.low.level.api.api.FirModuleResolveStateConfigurator
import org.jetbrains.kotlin.load.kotlin.PackagePartProvider
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.js.JsPlatforms.defaultJsPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms.unspecifiedJvmPlatform
import org.jetbrains.kotlin.platform.konan.NativePlatforms.unspecifiedNativePlatform
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import java.nio.file.Path
import java.nio.file.Paths

class FirModuleResolveStateConfiguratorImpl(
    private val project: Project,
    private val languageVersionSettings: LanguageVersionSettings,
    private val ktFiles: List<KtFile>
) :
    FirModuleResolveStateConfigurator() {
    override fun createPackagePartsProvider(moduleInfo: ModuleSourceInfoBase, scope: GlobalSearchScope): PackagePartProvider =
        PackagePartProvider.Empty

    //todo make better with FirModuleInfoBasedModuleData
    override fun createModuleDataProvider(moduleInfo: ModuleSourceInfoBase): ModuleDataProvider {
        return SingleModuleDataProvider(
            FirModuleDataImpl(
                moduleInfo.name,
                emptyList(),
                emptyList(),
                emptyList(),
                moduleInfo.platform,
                moduleInfo.analyzerServices
            )
        )
    }

    override fun getLanguageVersionSettings(moduleInfo: ModuleSourceInfoBase): LanguageVersionSettings = languageVersionSettings

    override fun getModuleSourceScope(moduleInfo: ModuleSourceInfoBase): GlobalSearchScope =
        //todo fix list of KtFiles
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
            unspecifiedJvmPlatform.single(),
            defaultJsPlatform.single(),
            unspecifiedNativePlatform.single()
        )
    )
) : ModuleSourceInfoBase {
    override fun dependencies() = listOf(this)

    override fun modulesWhoseInternalsAreVisible(): Collection<ModuleInfo> = emptyList()

    override val analyzerServices: PlatformDependentAnalyzerServices
        get() = CommonPlatformAnalyzerServices
}