/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.utils

import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path

fun findSourceFiles(workspaceRoot: Path): Set<URI> {
    val sourceMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.{kt}")
    return workspaceRoot.toFile()
        .walk()
        .map { it.toPath() }
        .filter { sourceMatcher.matches(it) }
        .map { it.toUri() }
        .toSet()
}