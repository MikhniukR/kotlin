/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lsp.utils.findSourceFiles
import org.jetbrains.kotlin.lsp.utils.toOffset
import org.jetbrains.kotlin.psi.KtFile
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

class SourceFilesManager(val serviceManager: ServiceManager) {

    private val workspaceRoots = mutableSetOf<Path>()
    private val sourceFiles = mutableMapOf<URI, SourceKtFile>()

    fun addWorkspaceRoot(root: Path) {
        workspaceRoots.add(root)
        val newSource = findSourceFiles(root)

        for (uri in newSource) {
            SourceKtFile(uri, serviceManager.getPsiFactory()).let {
                if (it.language == KotlinLanguage.INSTANCE) sourceFiles[uri] = it
            }
        }
    }

    fun getKtFile(uri: URI): KtFile? =
        sourceFiles[uri]?.ktFile

    fun getAllKtFiles(): List<KtFile> = sourceFiles.values.map { it.ktFile }

    fun editFile(uri: URI, changes: List<TextDocumentContentChangeEvent>) {
        val document = getDocument(uri) ?: return
        val project = serviceManager.getProject()
        addListener(document, project)
        WriteAction.run<Exception> {
            CommandProcessor.getInstance().executeCommand(project, {
                changes.forEach {
                    if (it.range.start.toOffset(document) != it.range.end.toOffset(document)) {
                        document.deleteString(it.range.start.toOffset(document), it.range.end.toOffset(document))
                    }
                    document.insertString(it.range.start.toOffset(document), it.text)
                    PsiDocumentManager.getInstance(project).commitDocument(document)
                }
            }, "Edit for didChange", "")
        }
    }

    fun editFileHardRebuildPSI(uri: URI, changes: List<TextDocumentContentChangeEvent>) {
        val ktFile = getKtFile(uri) ?: return
        var text = StringBuilder(ktFile.text)
        changes.forEach {
            if (it.range.start.toOffset(ktFile) != it.range.end.toOffset(ktFile)) {
                text = StringBuilder(text.removeRange(it.range.start.toOffset(ktFile), it.range.end.toOffset(ktFile)))
            }
            text.insert(it.range.start.toOffset(ktFile), it.text)
        }

        sourceFiles.replace(uri, SourceKtFile(uri, serviceManager.getPsiFactory(), text.toString()))
        serviceManager.updateServices(getAllKtFiles())
    }

    private fun getDocument(uri: URI): Document? {
        return sourceFiles[uri]?.getDocument()
    }

    private fun addListener(document: Document, project: Project) {
        // Do many times, because:
        // Document instances are weakly referenced from VirtualFile instances.
        // Thus, an unmodified Document instance can be garbage-collected if no
        // one references it, and a new instance is created if the document contents are reaccessed later.
        try {
            document.addDocumentListener(PsiDocumentManager.getInstance(project) as DocumentListener)
        } catch (throwable: Throwable) {
        }
    }

}

val URI.filePath: Path? get() = runCatching { Paths.get(this) }.getOrNull()
