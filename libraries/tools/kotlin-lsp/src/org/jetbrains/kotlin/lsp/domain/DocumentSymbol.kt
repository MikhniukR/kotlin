/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.domain

import org.jetbrains.kotlin.psi.*

data class DocumentSymbol(
    val name: String,
    val detail: String?,
    val kind: SymbolKind,
    val tags: Collection<SymbolTag>?,
//     deprecated Use tags instead, but tags is new from 3.16.0, so need to support also this
    val deprecated: Boolean?,
    val range: Range,
    val selectionRange: Range,
    val children: MutableList<DocumentSymbol>
)
