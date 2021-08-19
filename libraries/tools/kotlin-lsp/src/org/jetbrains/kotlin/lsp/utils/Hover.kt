/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.utils

import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position
import org.jetbrains.kotlin.idea.frontend.api.analyseWithReadAction
import org.jetbrains.kotlin.idea.frontend.api.symbols.*
import org.jetbrains.kotlin.idea.frontend.api.symbols.markers.KtAnnotatedSymbol
import org.jetbrains.kotlin.idea.frontend.api.symbols.markers.KtNamedSymbol
import org.jetbrains.kotlin.idea.frontend.api.symbols.markers.KtSymbolWithTypeParameters
import org.jetbrains.kotlin.idea.frontend.api.symbols.markers.KtSymbolWithVisibility
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifier

fun doHover(position: Position, ktFile: KtFile): Hover? {
    val reference = ktFile.findReferenceAt(position.toOffset(ktFile)) ?: return null
    if (reference !is KtReference)
        return null

    var hoverText = ""
    analyseWithReadAction(ktFile) {
        val symbol = reference.resolveToSymbol()
        hoverText = makeHover(symbol!!)
    }

    return Hover(MarkupContent("markdown", "```kotlin\n$hoverText\n```"))
}

fun makeHover(symbol: KtSymbol): String {
    var result = ""
    if (symbol is KtAnnotatedSymbol) {
        result += symbol.annotations.joinToString(separator = "\n") { it.psi!!.text }
    }
    if (symbol is KtSymbolWithVisibility) {
        result += symbol.visibility.name + " "
    }
    if (symbol is KtSymbolWithTypeParameters && symbol.typeParameters.isNotEmpty()) {
        //because of <T : SomeClass>
        result += symbol.typeParameters.joinToString(prefix = "<", postfix = "> ", separator = ", ") { it.psi!!.text }
    }

    if (symbol.psi is KtNamedFunction) {
        result += "fun "
    } else if (symbol.psi is KtConstructor<*>) {
        result += "constructor "
    }

    result += if (symbol is KtNamedSymbol) {
        symbol.name.toString()
    } else {
        (symbol.psi as KtConstructor<*>).name
    }
    if (symbol is KtFunctionLikeSymbol) {
        result += symbol.valueParameters.joinToString(prefix = "(", postfix = ")", separator = ", ") { it.psi!!.text }
    }
    if (symbol.psi is KtNamedFunction && (symbol.psi as KtNamedFunction).typeReference != null)
        result += ": " + (symbol.psi as KtNamedFunction).typeReference?.text
    else if (symbol.psi is KtNamedFunction)
        result += ": Unit"

    return result
}
