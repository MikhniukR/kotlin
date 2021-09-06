/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import com.intellij.core.CoreApplicationEnvironment
import com.intellij.formatting.Formatter
import com.intellij.formatting.FormatterImpl
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.AsyncExecutionService
import com.intellij.openapi.application.impl.AsyncExecutionServiceImpl
import com.intellij.openapi.editor.impl.DocumentWriteAccessGuard
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.fileEditor.impl.FileEditorPsiTreeChangeListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.pom.PomModel
import com.intellij.pom.tree.TreeAspect
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.DocumentCommitProcessor
import com.intellij.psi.impl.DocumentCommitThread
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.config.CompilerConfiguration
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
import org.jetbrains.kotlin.lsp.implementations.*
import org.jetbrains.kotlin.psi.KotlinReferenceProvidersService
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

class ServiceManager {

    private var environment: KotlinCoreEnvironment
    private var project: MockProject

    private var factory: PsiFileFactoryImpl

    init {
        val jarsPaths = listOf(
            "/Users/Roman.Mikhniuk/work/kotlin/libraries/stdlib/jvm/build/libs/kotlin-stdlib-1.6.255-SNAPSHOT.jar",
            "/Users/Roman.Mikhniuk/work/kotlin/core/builtins/build/libs/builtins-1.6.255-SNAPSHOT.jar",
            "/Users/Roman.Mikhniuk/.gradle/wrapper/dists/gradle-6.9-bin/2ecsmyp3bolyybemj56vfn4mt/gradle-6.9/lib/kotlin-stdlib-1.4.20.jar",
            "/Users/Roman.Mikhniuk/.gradle/wrapper/dists/gradle-6.9-bin/2ecsmyp3bolyybemj56vfn4mt/gradle-6.9/lib/kotlin-stdlib-common-1.4.20.jar",
            "/Users/Roman.Mikhniuk/.gradle/wrapper/dists/gradle-6.9-bin/2ecsmyp3bolyybemj56vfn4mt/gradle-6.9/lib/kotlin-stdlib-jdk7-1.4.20.jar",
            "/Users/Roman.Mikhniuk/.gradle/wrapper/dists/gradle-6.9-bin/2ecsmyp3bolyybemj56vfn4mt/gradle-6.9/lib/kotlin-stdlib-jdk8-1.4.20.jar",

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

        val compilerConfiguration = CompilerConfiguration()
        jarsPaths.map { JvmClasspathRoot(File(it), false) }
            .forEach { compilerConfiguration.add(CLIConfigurationKeys.CONTENT_ROOTS, it) }
        compilerConfiguration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        environment = KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable(), compilerConfiguration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )

        project = environment.project as MockProject
        factory = PsiFileFactoryImpl(project)
    }

    fun getPsiFactory(): PsiFileFactoryImpl = factory

    fun getProject(): Project = project

    @OptIn(InvalidWayOfUsingAnalysisSession::class)
    fun registerComponents(ktFiles: List<KtFile>) {

        project.picoContainer.registerComponentInstance(
            KtAnalysisSessionProvider::class.qualifiedName,
            KtFirAnalysisSessionProvider(project)
        )

        project.picoContainer.registerComponentInstance(
            FirModuleResolveStateConfigurator::class.qualifiedName,
            FirModuleResolveStateConfiguratorImpl(project, ktFiles, environment)
        )

        project.picoContainer.registerComponentInstance(
            KotlinOutOfBlockModificationTrackerFactory::class.qualifiedName,
            KotlinOutOfBlockModificationTrackerFactoryImpl()
        )

        RegisterComponentServiceHack.registerFirIdeResolveStateService(project)

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
            //to edit
            application.registerService(DocumentCommitProcessor::class.java, DocumentCommitThread::class.java)
            application.registerService(AsyncExecutionService::class.java, AsyncExecutionServiceImpl::class.java)
            application.registerService(Formatter::class.java, FormatterImpl::class.java)
        }

        //to edit
        project.picoContainer.registerComponentInstance(
            CodeStyleManager::class.qualifiedName,
            CodeStyleManagerImpl(project)
        )
        project.picoContainer.registerComponentInstance(
            TreeAspect::class.qualifiedName,
            TreeAspect()
        )
        project.picoContainer.registerComponentInstance(
            PomModel::class.qualifiedName,
            HackPomModel(project)
        )

        //to edit
        CoreApplicationEnvironment.registerExtensionPoint(
            Extensions.getRootArea(),
            DocumentWriteAccessGuard.EP_NAME,
            DocumentWriteAccessGuard::class.java
        )
        CoreApplicationEnvironment.registerExtensionPoint(
            project.extensionArea,
            PsiTreeChangeListener.EP,
            FileEditorPsiTreeChangeListener::class.java
        )
    }

    fun updateServices(ktFiles: List<KtFile>) {
        project.picoContainer.unregisterComponent(FirModuleResolveStateConfigurator::class.qualifiedName)
        project.picoContainer.registerComponentInstance(
            FirModuleResolveStateConfigurator::class.qualifiedName,
            FirModuleResolveStateConfiguratorImpl(project, ktFiles, environment)
        )

        project.picoContainer.unregisterComponent(KtDeclarationProviderFactory::class.qualifiedName)
        project.picoContainer.registerComponentInstance(
            KtDeclarationProviderFactory::class.qualifiedName,
            object : KtDeclarationProviderFactory() {
                override fun createDeclarationProvider(searchScope: GlobalSearchScope): DeclarationProvider {
                    return DeclarationProviderImpl(searchScope, ktFiles.filter { searchScope.contains(it.virtualFile) })
                }
            })

        project.picoContainer.unregisterComponent(KtPackageProviderFactory::class.qualifiedName)
        project.picoContainer.registerComponentInstance(
            KtPackageProviderFactory::class.qualifiedName,
            object : KtPackageProviderFactory() {
                override fun createPackageProvider(searchScope: GlobalSearchScope): KtPackageProvider {
                    return KtPackageProviderImpl(searchScope, ktFiles.filter { searchScope.contains(it.virtualFile) })
                }
            })

    }
}