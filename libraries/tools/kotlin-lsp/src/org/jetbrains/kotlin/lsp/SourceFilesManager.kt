/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.idea.fir.low.level.api.DeclarationProvider
import org.jetbrains.kotlin.idea.fir.low.level.api.KtDeclarationProviderFactory
import org.jetbrains.kotlin.idea.fir.low.level.api.KtPackageProvider
import org.jetbrains.kotlin.idea.fir.low.level.api.KtPackageProviderFactory
import org.jetbrains.kotlin.idea.fir.low.level.api.api.FirModuleResolveStateConfigurator
import org.jetbrains.kotlin.idea.fir.low.level.api.api.KotlinOutOfBlockModificationTrackerFactory
import org.jetbrains.kotlin.idea.frontend.api.InvalidWayOfUsingAnalysisSession
import org.jetbrains.kotlin.idea.frontend.api.KtAnalysisSessionProvider
import org.jetbrains.kotlin.idea.frontend.api.fir.KtFirAnalysisSessionProvider
import org.jetbrains.kotlin.idea.references.HLApiReferenceProviderService
import org.jetbrains.kotlin.idea.references.KotlinFirReferenceContributor
import org.jetbrains.kotlin.idea.references.KotlinReferenceProviderContributor
import org.jetbrains.kotlin.lsp.implementations.DeclarationProviderImpl
import org.jetbrains.kotlin.lsp.implementations.FirModuleResolveStateConfiguratorImpl
import org.jetbrains.kotlin.lsp.implementations.KotlinOutOfBlockModificationTrackerFactoryImpl
import org.jetbrains.kotlin.lsp.implementations.KtPackageProviderImpl
import org.jetbrains.kotlin.lsp.utils.findSourceFiles
import org.jetbrains.kotlin.psi.KotlinReferenceProvidersService
import org.jetbrains.kotlin.psi.KtFile
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

class SourceFilesManager {

    private val workspaceRoots = mutableSetOf<Path>()
    private val sourceFiles = mutableMapOf<URI, SourceKtFile>()

    private val environment = KotlinCoreEnvironment.createForProduction(
        Disposer.newDisposable(), CompilerConfiguration(),
        EnvironmentConfigFiles.JVM_CONFIG_FILES
    )
    private val project = environment.project as MockProject

    private val factory = PsiFileFactoryImpl(project)

    fun addWorkspaceRoot(root: Path) {
        workspaceRoots.add(root)
        val newSource = findSourceFiles(root)

        for (uri in newSource) {
            SourceKtFile(uri, factory).let {
                sourceFiles[uri] = it
            }
        }
    }

    fun getKtFile(uri: URI): KtFile? =
        sourceFiles[uri]?.ktFile

    fun getAllKtFiles(): List<KtFile> = sourceFiles.values.map { it.ktFile }

    @OptIn(InvalidWayOfUsingAnalysisSession::class)
    fun registerComponents() {
        val ktFiles = sourceFiles.values.map { it.ktFile }

        project.picoContainer.registerComponentInstance(
            KtAnalysisSessionProvider::class.qualifiedName,
            KtFirAnalysisSessionProvider(project)
        )
        //todo check to resolve from stdlib
        project.picoContainer.registerComponentInstance(
            FirModuleResolveStateConfigurator::class.qualifiedName,
            FirModuleResolveStateConfiguratorImpl(project, environment.configuration.languageVersionSettings, ktFiles)
        )

        project.picoContainer.registerComponentInstance(
            KotlinOutOfBlockModificationTrackerFactory::class.qualifiedName,
            KotlinOutOfBlockModificationTrackerFactoryImpl()
        )

        RegisterComponentService.registerFirIdeResolveStateService(project)

        project.picoContainer.registerComponentInstance(
            KtDeclarationProviderFactory::class.qualifiedName,
            object : KtDeclarationProviderFactory() {
                override fun createDeclarationProvider(searchScope: GlobalSearchScope): DeclarationProvider {
                    return DeclarationProviderImpl(searchScope, ktFiles.filter { searchScope.contains(it.virtualFile) })
                }
            })

        project.picoContainer.registerComponentInstance(
            KtPackageProviderFactory::class.qualifiedName,
            object : KtPackageProviderFactory() {
                override fun createPackageProvider(searchScope: GlobalSearchScope): KtPackageProvider {
                    return KtPackageProviderImpl(searchScope, ktFiles.filter { searchScope.contains(it.virtualFile) })
                }
            })

        val application = ApplicationManager.getApplication() as MockApplication
        KotlinCoreEnvironment.underApplicationLock {
            application.registerService(KotlinReferenceProvidersService::class.java, HLApiReferenceProviderService::class.java)
            application.registerService(KotlinReferenceProviderContributor::class.java, KotlinFirReferenceContributor::class.java)
        }

    }
}

val URI.filePath: Path? get() = runCatching { Paths.get(this) }.getOrNull()
