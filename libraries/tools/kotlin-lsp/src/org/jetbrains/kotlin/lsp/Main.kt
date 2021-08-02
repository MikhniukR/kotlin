/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import com.intellij.mock.MockProject
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.fir.low.level.api.*
import org.jetbrains.kotlin.idea.fir.low.level.api.api.FirModuleResolveStateConfigurator
import org.jetbrains.kotlin.idea.fir.low.level.api.api.KotlinOutOfBlockModificationTrackerFactory
import org.jetbrains.kotlin.idea.frontend.api.InvalidWayOfUsingAnalysisSession
import org.jetbrains.kotlin.idea.frontend.api.KtAnalysisSessionProvider
import org.jetbrains.kotlin.idea.frontend.api.analyseWithReadAction
import org.jetbrains.kotlin.idea.frontend.api.fir.KtFirAnalysisSessionProvider
import org.jetbrains.kotlin.idea.references.*
import org.jetbrains.kotlin.lsp.implementations.DeclarationProviderImpl
import org.jetbrains.kotlin.lsp.implementations.FirModuleResolveStateConfiguratorImpl
import org.jetbrains.kotlin.lsp.implementations.KotlinOutOfBlockModificationTrackerFactoryImpl
import org.jetbrains.kotlin.lsp.implementations.KtPackageProviderImpl
import org.jetbrains.kotlin.psi.KotlinReferenceProvidersService
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import java.net.URLClassLoader

@OptIn(InvalidWayOfUsingAnalysisSession::class)
fun registerComponents(project: MockProject, environment: KotlinCoreEnvironment, ktFiles: List<KtFile>) {
    //do only once

    project.registerService(KtAnalysisSessionProvider::class.java, KtFirAnalysisSessionProvider::class.java)
//    project.picoContainer.registerComponentInstance(
//        KtAnalysisSessionProvider::class.qualifiedName,
//        KtFirAnalysisSessionProvider(project)
//    )
    project.picoContainer.registerComponentInstance(
        FirModuleResolveStateConfigurator::class.qualifiedName,
        FirModuleResolveStateConfiguratorImpl(project, environment.configuration.languageVersionSettings, ktFiles)
    )
//    val firIdeResolveStateService = createResolveStateForNoCaching(ModuleInfoImpl(), project)
//    project.picoContainer.registerComponentInstance(
//        firIdeResolveStateService::class.qualifiedName,
//        firIdeResolveStateService
//    )

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
                //todo fix list of KtFiles
//                return DeclarationProviderImpl(searchScope, emptyList())
            }
        })

    project.picoContainer.registerComponentInstance(
        KtPackageProviderFactory::class.qualifiedName,
        object : KtPackageProviderFactory() {
            override fun createPackageProvider(searchScope: GlobalSearchScope): KtPackageProvider {
                return KtPackageProviderImpl(searchScope, ktFiles.filter { searchScope.contains(it.virtualFile) })
                //todo fix list of KtFiles
//                return KtPackageProviderImpl(searchScope, emptyList())
            }
        })


    project.registerService(KotlinReferenceProvidersService::class.java, HLApiReferenceProviderService::class.java)
    project.registerService(KotlinReferenceProviderContributor::class.java, KotlinFirReferenceContributor::class.java)
}


fun createKtFile(path: String): KtFile {
    val shortName = path.split("/").last()
    val text = FileUtil.loadFile(
        File(path),
        CharsetToolkit.UTF8,
        true
    ).trim { it <= ' ' }

    val environment = KotlinCoreEnvironment.createForProduction(
        Disposer.newDisposable(), CompilerConfiguration(),
        EnvironmentConfigFiles.JVM_CONFIG_FILES
    )
    val project = environment.project as MockProject

    val factory = PsiFileFactoryImpl(project)

    val virtualFile = LightVirtualFile(shortName, KotlinLanguage.INSTANCE, StringUtilRt.convertLineSeparators(text))
    virtualFile.charset = CharsetToolkit.UTF8_CHARSET

    val ktFile = factory.trySetupPsiForFile(virtualFile, KotlinLanguage.INSTANCE, true, false) as KtFile

    registerComponents(project, environment, listOf(ktFile))

    return ktFile
}

fun getSomeElement(ktFile: KtFile): KtElement {
    return ktFile.children[2].children[1].children[0].children[0].children[0] as KtElement
}

//для комплишна, получить все деклорации в текущем месте
fun tryGetScopeContext(ktFile: KtFile) {
    analyseWithReadAction(ktFile) {
        var scopes = ktFile.getScopeContextForPosition(getSomeElement(ktFile)).scopes
        println(scopes)
    }
}

fun findSomeReference(ktFile: KtFile): KtReference? {
    for (i in 1..300) {
        val reference = ktFile.findReferenceAt(i)
        if (reference != null && reference is KtReference)
            return reference
    }

    return null
}

fun tryResolve(ktFile: KtFile) {
    analyseWithReadAction(ktFile) {
        val mainRef = ktFile.mainReference
        val reference = findSomeReference(ktFile)
        reference?.resolveToSymbol()
    }
}

fun main() {
    val fullPath = "/Users/Roman.Mikhniuk/work/kotlin/libraries/tools/kotlin-lsp/testData/test.kt"
    val ktFile = createKtFile(fullPath)
    val ktElement = getSomeElement(ktFile)
    val d =   (Thread.currentThread().getContextClassLoader() as URLClassLoader).getURLs()
//    tryGetScopeContext(ktFile)
//    tryGetScopeContext(ktElement)
    tryResolve(ktFile)
//    val result = doDocumentSymbol(ktFile)
//    result.forEach { println(it) }
}

///Users/Roman.Mikhniuk/work/kotlin/libraries/stdlib/jvm/build/libs/kotlin-stdlib-1.6.255-SNAPSHOT.jar!/kotlin/kotlin.kotlin_builtins