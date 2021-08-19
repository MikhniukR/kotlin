/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.*
import org.jetbrains.kotlin.lsp.domain.Configuration
import org.jetbrains.kotlin.lsp.utils.AsyncExecutor
import java.io.Closeable
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

class KotlinLanguageServer : LanguageServer, LanguageClientAware, Closeable {

    private val config = Configuration()
    private val async = AsyncExecutor()
    private val sourceFiles = SourceFilesManager()

    private val textDocumentService = KotlinTextDocumentService(sourceFiles)
    private val workspaceService = KotlinWorkspaceService(sourceFiles)
    private lateinit var client: LanguageClient

    companion object {
        const val VERSION: String = "0.1"//System.getProperty("kotlinLanguageServer.version")
    }

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> = async.compute {
        val serverCapabilities = ServerCapabilities()
        serverCapabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental)
        serverCapabilities.workspace = WorkspaceServerCapabilities()
        serverCapabilities.workspace.workspaceFolders = WorkspaceFoldersOptions()
        serverCapabilities.workspace.workspaceFolders.supported = false
        serverCapabilities.workspace.workspaceFolders.changeNotifications = Either.forRight(false)
        serverCapabilities.hoverProvider = Either.forLeft(true)
        serverCapabilities.completionProvider = CompletionOptions(false, listOf("."))
        serverCapabilities.signatureHelpProvider = SignatureHelpOptions(listOf("(", ","))
        serverCapabilities.definitionProvider = Either.forLeft(true)
        serverCapabilities.documentSymbolProvider = Either.forLeft(true)
        serverCapabilities.workspaceSymbolProvider = Either.forLeft(true)
        serverCapabilities.referencesProvider = Either.forLeft(true)
//        serverCapabilities.semanticTokensProvider = SemanticTokensWithRegistrationOptions()
        serverCapabilities.codeActionProvider = Either.forLeft(false)
        serverCapabilities.documentFormattingProvider = Either.forLeft(false)
        serverCapabilities.documentRangeFormattingProvider = Either.forLeft(false)
//        serverCapabilities.executeCommandProvider = ExecuteCommandOptions(ALL_COMMANDS)

        val clientCapabilities = params.capabilities
        config.completion.snippets.enabled = clientCapabilities?.textDocument?.completion?.completionItem?.snippetSupport ?: false

//        if (clientCapabilities?.window?.workDoneProgress ?: false) {
//            progressFactory = LanguageClientProgress.Factory(client)
//        }

        @Suppress("DEPRECATION")
        val folders = params.workspaceFolders?.takeIf { it.isNotEmpty() }
            ?: params.rootUri?.let(::WorkspaceFolder)?.let(::listOf)
            ?: params.rootPath?.let(Paths::get)?.toUri()?.toString()?.let(::WorkspaceFolder)?.let(::listOf)
            ?: listOf()

//        val progress = params.workDoneToken?.let { LanguageClientProgress("Workspace folders", it, client) }

        folders.forEach { folder ->
//            LOG.info("Adding workspace folder {}", folder.name)
//            val progressPrefix = "[${i + 1}/${folders.size}] ${folder.name}"
//            val progressPercent = (100 * i) / folders.size

//            progress?.update("$progressPrefix: Updating source path", progressPercent)
            val root = Paths.get(URI(folder.uri))
            sourceFiles.addWorkspaceRoot(root)

//            progress?.update("$progressPrefix: Updating class path", progressPercent)
//            val refreshed = classPath.addWorkspaceRoot(root)
//            if (refreshed) {
//                progress?.update("$progressPrefix: Refreshing source path", progressPercent)
//                sourcePath.refresh()
//            }
        }

        //todo move somewhere shouldn't be here
        sourceFiles.registerComponents()

        val serverInfo = ServerInfo("Kotlin Language Server", VERSION)


        InitializeResult(serverCapabilities, serverInfo)
    }

    override fun shutdown(): CompletableFuture<Any> {
        close()
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {}

    override fun getTextDocumentService(): TextDocumentService = textDocumentService

    override fun getWorkspaceService(): WorkspaceService = workspaceService

    override fun close() {
        async.shutdown(awaitTermination = true)
    }

    override fun connect(client: LanguageClient) {
        this.client = client
    }
}