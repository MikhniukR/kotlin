/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.utils

import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.SymbolKind
import org.jetbrains.kotlin.psi.*

fun doDocumentSymbol(element: KtElement): List<DocumentSymbol> {
    val children = element.children.filterIsInstance<KtElement>().flatMap { doDocumentSymbol(it) }

    return pickImportantElements(element)?.let {
        mutableListOf(documentSymbol(element as KtNamedDeclaration, children.toMutableList()))
    } ?: children
}

private fun pickImportantElements(element: KtElement): KtNamedDeclaration? =
    when (element) {
        is KtClassOrObject -> if (element.name == null) null else element
        is KtTypeAlias -> element
        is KtConstructor<*> -> element
        is KtNamedFunction -> element
        is KtProperty -> element
        is KtVariableDeclaration -> element
        else -> null
    }

fun documentSymbol(declaration: KtNamedDeclaration, children: MutableList<DocumentSymbol>): DocumentSymbol {
    val range = declaration.textRange.toLspRange(declaration)

    return DocumentSymbol(
        declaration.name ?: "<anonymous>",
        symbolKind(declaration),
        range,
        declaration.nameIdentifier?.textRange?.toLspRange(declaration) ?: range,
        null,
        children
    )
}

fun symbolKind(namedDeclaration: KtNamedDeclaration): SymbolKind =
    when (namedDeclaration) {
        is KtClassOrObject -> SymbolKind.Class
        is KtTypeAlias -> SymbolKind.Interface
        is KtConstructor<*> -> SymbolKind.Constructor
        is KtNamedFunction -> SymbolKind.Function
        is KtProperty -> SymbolKind.Property
        //todo diff Property vs Variable
//        is KtVariableDeclaration -> SymbolKind.Variable
        else -> throw IllegalArgumentException("Unexpected symbol $namedDeclaration")
    }

fun DocumentSymbol.getAsList(): MutableList<DocumentSymbol> {
    if (children.isEmpty())
        return mutableListOf(this)

    val result = mutableListOf(this)
    children.forEach { result.addAll(it.getAsList()) }

    return result
}