/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.domain

enum class SymbolTag(val value: Int) {
    Deprecated(1);


    fun forValue(value: Int): SymbolTag {
        if (value in SymbolTag.values().indices)
            throw IllegalArgumentException("Illegal enum value: $value")
        return SymbolTag.values()[value - 1]
    }
}