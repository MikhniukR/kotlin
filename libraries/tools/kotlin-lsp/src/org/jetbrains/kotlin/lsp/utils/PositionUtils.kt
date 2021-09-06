/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.utils

import com.intellij.openapi.editor.Document
import org.eclipse.lsp4j.Position
import org.jetbrains.kotlin.psi.KtFile

fun Int.toPosition(ktFile: KtFile): Position {
    val line = ktFile.text.subSequence(0, this).count { c -> c == '\n' }
    var character = this

    ktFile.text.split('\n').subList(0, line).forEach { character -= it.length }

    return Position(line, (character - line))
}

fun Position.toOffset(ktFile: KtFile): Int {
    return toOffset(ktFile.text)
}

fun Position.toOffset(document: Document): Int {
    return toOffset(document.text)
}

private fun Position.toOffset(text: String): Int {
    var result = 0
    text.split('\n').subList(0, line).forEach { result += it.length }
    return result + character + line
}
