/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.TextDocumentService
import org.jetbrains.kotlin.lsp.definition.doGoToDefinition
import org.jetbrains.kotlin.lsp.hover.doHover
import org.jetbrains.kotlin.lsp.reference.findReferences
import org.jetbrains.kotlin.lsp.symbols.doDocumentSymbol
import org.jetbrains.kotlin.lsp.utils.*
import java.io.Closeable
import java.io.File
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture

class KotlinTextDocumentService(private val filesManager: SourceFilesManager) : TextDocumentService, LanguageClientAware, Closeable {

    private val async = AsyncExecutor()

    private lateinit var client: LanguageClient

    override fun definition(params: DefinitionParams): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>>? =
        async.compute {
            val uri = URI(params.textDocument.uri)
            val position = params.position
            val ktFile = filesManager.getKtFile(uri)

            Either.forLeft(ktFile?.let {
                doGoToDefinition(position, it)
                    ?.map { symbol -> Location(symbol.uri, symbol.range) }
                    ?.toMutableList()
            } ?: mutableListOf())
        }


    override fun documentSymbol(params: DocumentSymbolParams): CompletableFuture<MutableList<Either<SymbolInformation, DocumentSymbol>>> =
        async.compute {
            val uri = URI(params.textDocument.uri)
            val ktFile = filesManager.getKtFile(uri)

            ktFile?.let {
                doDocumentSymbol(it)
                    .map { Either.forRight<SymbolInformation, DocumentSymbol>(it) }
                    .toMutableList()
            } ?: mutableListOf()
        }

    override fun hover(params: HoverParams): CompletableFuture<Hover> = async.compute {
        val uri = URI(params.textDocument.uri)
        val position = params.position
        val ktFile = filesManager.getKtFile(uri)

        ktFile?.let { doHover(position, it) }
    }

    override fun references(params: ReferenceParams): CompletableFuture<MutableList<out Location>> = async.compute {
        val uri = URI(params.textDocument.uri)
        val position = params.position
        val ktFile = filesManager.getKtFile(uri)

        findReferences(position, ktFile!!, filesManager)
    }

    override fun didOpen(params: DidOpenTextDocumentParams?) {
        TODO()
    }

    override fun didChange(params: DidChangeTextDocumentParams?) {
        val uri = URI.create(params?.textDocument?.uri ?: "")
        val changes = params?.contentChanges ?: return

        filesManager.editFile(uri, changes)
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        TODO()
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
        TODO()
    }

    override fun close() {
        async.shutdown(awaitTermination = true)
    }

    override fun connect(client: LanguageClient) {
        this.client = client
    }

    private fun log(text: String) {
        File("/Users/Roman.Mikhniuk/tmp/lsp.log").appendText("${Date(System.currentTimeMillis())} $text\n")
    }

}
