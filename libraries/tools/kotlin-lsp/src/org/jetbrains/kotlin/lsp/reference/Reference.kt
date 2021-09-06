/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.reference

import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.Position
import org.jetbrains.kotlin.idea.frontend.api.analyseWithReadAction
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lsp.SourceFilesManager
import org.jetbrains.kotlin.lsp.utils.toLspRange
import org.jetbrains.kotlin.lsp.utils.toOffset
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtFile

fun findReferences(position: Position, ktFile: KtFile, filesManager: SourceFilesManager): MutableList<Location> {
    val offset = position.toOffset(ktFile)
    val element = ktFile.findElementAt(offset) ?: return mutableListOf()
    val result = mutableListOf<Location>()
    filesManager.getAllKtFiles().forEach { file ->
        file.text.indexesOf(element.text).forEach {
            val reference = file.findReferenceAt(it)
            if (reference != null && reference is KtReference) {
                analyseWithReadAction(ktFile) {
                    val symbol = reference.resolveToSymbol()
                    if (symbol != null && symbol.psi!!.containingFile.equals(ktFile) && (symbol.psi!!.textRange.contains(offset) ||
                                (symbol.psi is KtConstructor<*> && symbol.psi!!.parent.textRange.contains(offset)))
                    ) {
                        result.add(
                            Location(
                                file.virtualFilePath.substring(1), file.findElementAt(it)!!.textRange.toLspRange(file)
                            )
                        )
                    }
                }
            }
        }
    }

    return result
}

fun String.indexesOf(substr: String): List<Int> {
    return Regex(substr).findAll(this).map { it.range.first }.toList()
}