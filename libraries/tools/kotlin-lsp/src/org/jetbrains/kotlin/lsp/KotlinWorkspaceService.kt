/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.WorkspaceService
import org.jetbrains.kotlin.lsp.utils.AsyncExecutor
import org.jetbrains.kotlin.lsp.symbols.doDocumentSymbol
import org.jetbrains.kotlin.lsp.symbols.getAsList
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture

class KotlinWorkspaceService(private val sourceFiles: SourceFilesManager) : WorkspaceService, LanguageClientAware {

    private val async = AsyncExecutor()

    override fun symbol(params: WorkspaceSymbolParams?): CompletableFuture<MutableList<out SymbolInformation>> = async.compute {
        sourceFiles.getAllKtFiles()
            .flatMap { ktFile -> doDocumentSymbol(ktFile).flatMap { it.getAsList() }.map { Pair(ktFile, it) } }
            .map { (ktFile, symbol) ->
                SymbolInformation(
                    symbol.name,
                    symbol.kind,
                    Location(ktFile.virtualFilePath.substring(1), symbol.range)
                )
            }.toMutableList()
    }

    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
        TODO("Not yet implemented")
    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
        TODO("Not yet implemented")
    }

    override fun connect(client: LanguageClient?) {
        TODO("Not yet implemented")
    }
}