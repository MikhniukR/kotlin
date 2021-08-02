/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.utils

import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.psi.KtElement

fun KtElement.acceptAll(visitor: PsiElementVisitor) {
    this.accept(visitor)
    children.forEach { ch -> if (ch is KtElement) ch.acceptAll(visitor) }
}
