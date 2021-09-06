/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.*
import org.jetbrains.kotlin.lsp.utils.AsyncExecutor
import java.io.Closeable
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

class KotlinLanguageServer : LanguageServer, LanguageClientAware, Closeable {

    private val async = AsyncExecutor()
    private val serviceManager = ServiceManager()
    private val sourceFiles = SourceFilesManager(serviceManager)

    private val textDocumentService = KotlinTextDocumentService(sourceFiles)
    private val workspaceService = KotlinWorkspaceService(sourceFiles)
    private lateinit var client: LanguageClient

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> = async.compute {
        val serverCapabilities = ServerCapabilities()
        serverCapabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental)
        serverCapabilities.workspace = WorkspaceServerCapabilities()
        serverCapabilities.workspace.workspaceFolders = WorkspaceFoldersOptions()
        serverCapabilities.hoverProvider = Either.forLeft(true)
        serverCapabilities.signatureHelpProvider = SignatureHelpOptions(listOf("(", ","))
        serverCapabilities.definitionProvider = Either.forLeft(true)
        serverCapabilities.documentSymbolProvider = Either.forLeft(true)
        serverCapabilities.workspaceSymbolProvider = Either.forLeft(true)
        serverCapabilities.referencesProvider = Either.forLeft(true)

        @Suppress("DEPRECATION")
        val folders = params.workspaceFolders?.takeIf { it.isNotEmpty() }
            ?: params.rootUri?.let(::WorkspaceFolder)?.let(::listOf)
            ?: params.rootPath?.let(Paths::get)?.toUri()?.toString()?.let(::WorkspaceFolder)?.let(::listOf)
            ?: listOf()

        folders.forEach { folder ->
            val root = Paths.get(URI(folder.uri))
            sourceFiles.addWorkspaceRoot(root)
        }

        serviceManager.registerComponents(sourceFiles.getAllKtFiles())

        val serverInfo = ServerInfo("Kotlin Language Server")
        InitializeResult(serverCapabilities, serverInfo)
    }

    override fun shutdown(): CompletableFuture<Any> {
        close()
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {
        exitProcess(0)
    }

    override fun getTextDocumentService(): TextDocumentService = textDocumentService

    override fun getWorkspaceService(): WorkspaceService = workspaceService

    override fun close() {
        async.shutdown(awaitTermination = true)
        textDocumentService.close()
        //to exit in VS Code, in protocol exit should be on exit(it works in atom)
        exit()
    }

    override fun connect(client: LanguageClient) {
        this.client = client
    }
}