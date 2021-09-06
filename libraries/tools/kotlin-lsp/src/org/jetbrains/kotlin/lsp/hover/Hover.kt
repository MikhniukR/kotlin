/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.hover

import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position
import org.jetbrains.kotlin.idea.frontend.api.KtAnalysisSession
import org.jetbrains.kotlin.idea.frontend.api.analyseWithReadAction
import org.jetbrains.kotlin.idea.frontend.api.symbols.*
import org.jetbrains.kotlin.idea.frontend.api.symbols.markers.KtNamedSymbol
import org.jetbrains.kotlin.idea.frontend.api.symbols.markers.KtSymbolWithTypeParameters
import org.jetbrains.kotlin.idea.frontend.api.symbols.markers.KtSymbolWithVisibility
import org.jetbrains.kotlin.idea.frontend.api.types.KtType
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.lsp.utils.toOffset
import org.jetbrains.kotlin.psi.*

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

fun KtAnalysisSession.makeHover(symbol: KtSymbol): String {
    var result = ""

    if (symbol is KtSymbolWithVisibility) {
        result += symbol.visibility.name + " "
    }

    if (symbol is KtFunctionSymbol) {
        result += "fun "
    } else if (symbol is KtConstructorSymbol) {
        result += "constructor "
    }

    if (symbol is KtSymbolWithTypeParameters && symbol.typeParameters.isNotEmpty()) {
        result += symbol.typeParameters.joinToString(prefix = "<", postfix = "> ", separator = ", ") {
            if (it.upperBounds.isEmpty() || checkAny(it.upperBounds)) it.name.toString()
            else it.name.toString() + ": " + it.upperBounds.joinToString { type -> toHoverString(type) }
        }
    }

    result += if (symbol is KtNamedSymbol) {
        symbol.name.toString()
    } else {
        toHoverString((symbol as KtConstructorSymbol).annotatedType.type)
    }

    if (symbol is KtFunctionLikeSymbol) {
        result += symbol.valueParameters.joinToString(prefix = "(", postfix = ")", separator = ", ") { toHoverString(it) }
    }

    if (symbol !is KtConstructorSymbol) {
        result += ": " + toHoverString((symbol as KtCallableSymbol).annotatedType.type)
    }

    return result
}

private fun KtAnalysisSession.toHoverString(parameterSymbol: KtValueParameterSymbol): String {
    return parameterSymbol.name.toString() + ": " + toHoverString(parameterSymbol.annotatedType.type)
}

private fun KtAnalysisSession.checkAny(types: List<KtType>): Boolean {
    return types.size == 1 && checkAny(types[0])
}

private fun KtAnalysisSession.checkAny(ktType: KtType): Boolean {
    return toHoverString(ktType).contains("Any?")
}

private fun KtAnalysisSession.toHoverString(ktType: KtType): String {
    return ktType.render().removePackages()
}

private fun String.removePackages(): String {
    return this.replace(Regex("[^\\.<>]*\\."), "")
}
