/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.domain

import com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.psi.KtNamedDeclaration

data class Range(
    val start: Position,
    val end: Position
)

fun TextRange.toLspRange(declaration: KtNamedDeclaration): Range =
    Range(positionFromOffset(startOffset, declaration.containingKtFile), positionFromOffset(endOffset, declaration.containingKtFile))

