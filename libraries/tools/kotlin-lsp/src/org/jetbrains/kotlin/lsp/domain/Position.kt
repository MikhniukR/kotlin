/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.domain

import org.jetbrains.kotlin.psi.KtFile

data class Position(
    val line: UInt,
    val character: UInt
)

fun positionFromOffset(offset: Int, ktFile: KtFile): Position {
    val line = ktFile.text.subSequence(0, offset).count { c -> c == '\n' }
    var character = offset

    ktFile.text.split('\n').subList(0, line).forEach { str -> character -= str.length }

    return Position(line.toUInt(), (character - line).toUInt())
}
