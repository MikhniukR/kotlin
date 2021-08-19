/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.utils

import com.intellij.openapi.util.TextRange
import org.eclipse.lsp4j.Range
import org.jetbrains.kotlin.psi.KtElement

fun TextRange.toLspRange(declaration: KtElement): Range =
    Range(startOffset.toPosition(declaration.containingKtFile), endOffset.toPosition(declaration.containingKtFile))

