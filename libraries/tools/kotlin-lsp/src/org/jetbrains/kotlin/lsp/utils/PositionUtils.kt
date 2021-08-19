/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.utils

import org.eclipse.lsp4j.Position
import org.jetbrains.kotlin.psi.KtFile

//todo split only by \n looks bad
fun Int.toPosition(ktFile: KtFile): Position {
    val line = ktFile.text.subSequence(0, this).count { c -> c == '\n' }
    var character = this

    ktFile.text.split('\n').subList(0, line).forEach { character -= it.length }

    return Position(line, (character - line))
}

//map LSP Position to FIR offset
fun Position.toOffset(ktFile: KtFile): Int {
    var result = 0
    ktFile.text.split('\n').subList(0, line).forEach { result += it.length }
    return result + character + line
}
