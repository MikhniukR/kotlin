/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.definition

import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.jetbrains.kotlin.idea.frontend.api.analyseWithReadAction
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lsp.utils.toLspRange
import org.jetbrains.kotlin.lsp.utils.toOffset
import org.jetbrains.kotlin.psi.KtFile

fun doGoToDefinition(position: Position, ktFile: KtFile): MutableList<Location>? {
    val reference = ktFile.findReferenceAt(position.toOffset(ktFile)) ?: return null
    if (reference !is KtReference)
        return null

    var range: Range? = null
    var uri: String? = null
    analyseWithReadAction(ktFile) {
        val symbol = reference.resolveToSymbol()
        uri = symbol?.psi?.containingFile?.virtualFile?.name
        range = symbol?.psi?.textRange?.toLspRange(symbol.psi!!.containingFile as KtFile)
    }

    if (range == null || uri == null)
        return null

    return mutableListOf(Location(uri, range))
}