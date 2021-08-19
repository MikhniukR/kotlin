/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.LightVirtualFile
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.launch.LSPLauncher
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
import org.jetbrains.kotlin.lsp.implementations.*
import org.jetbrains.kotlin.lsp.utils.doHover
import org.jetbrains.kotlin.psi.*
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.Executors


@OptIn(InvalidWayOfUsingAnalysisSession::class)
fun registerComponents(project: MockProject, environment: KotlinCoreEnvironment, ktFiles: List<KtFile>) {
    //do only once

//    project.registerService(KtAnalysisSessionProvider::class.java, KtFirAnalysisSessionProvider::class.java)
    project.picoContainer.registerComponentInstance(
        KtAnalysisSessionProvider::class.qualifiedName,
        KtFirAnalysisSessionProvider(project)
    )
//    project.registerService(FirModuleResolveStateConfigurator::class.java, FirModuleResolveStateConfigurator::class.java)
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

    val application = ApplicationManager.getApplication() as MockApplication
    KotlinCoreEnvironment.underApplicationLock {
        application.registerService(KotlinReferenceProvidersService::class.java, HLApiReferenceProviderService::class.java)
        application.registerService(KotlinReferenceProviderContributor::class.java, KotlinFirReferenceContributor::class.java)
    }

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
    return ktFile.children[4] as KtElement
//    return ktFile.children[2].children[1].children[0].children[0].children[0] as KtElement
}

//для комплишна, получить все деклорации в текущем месте,
fun tryGetScopeContext(ktFile: KtFile, ktElement: KtElement) {
    analyseWithReadAction(ktFile) {
        var scope = ktFile.getScopeContextForPosition(ktElement).scopes
        println(scope)
        println(scope.getAllSymbols())
        println(scope.getAllPossibleNames())
    }
}

fun main(argv: Array<String>) {
//    val fullPath = "/Users/Roman.Mikhniuk/work/kotlin/libraries/tools/kotlin-lsp/testData/projects/simpleProject/src/test.kt"
    val fullPath = "/Users/Roman.Mikhniuk/work/kotlin/libraries/tools/kotlin-lsp/testData/projects/testSignatureHelp/src/test.kt"
    val ktFile = createKtFile(fullPath)
//    tryGetScopeContext(ktFile, ktFile.children[2].lastChild.children[0] as KtElement)
//    println(doHover(Position(7, 9), ktFile))
    println(doHover(Position(1, 6), ktFile))
//    startLSPServer()
}

fun startLSPServer() {
    val (inStream, outStream) = Pair(System.`in`, System.out)
//    val (inStream, outStream) = tcpConnectToClient("localhost", 8081)

    val server = KotlinLanguageServer()
    val threads = Executors.newSingleThreadExecutor { Thread(it, "client") }
    val launcher = LSPLauncher.createServerLauncher(server, inStream, outStream, threads) { it }

    server.connect(launcher.remoteProxy)
    launcher.startListening()
}

fun tcpConnectToClient(host: String, port: Int): Pair<InputStream, OutputStream> =
    Socket(host, port)
        .let { Pair(it.inputStream, it.outputStream) }