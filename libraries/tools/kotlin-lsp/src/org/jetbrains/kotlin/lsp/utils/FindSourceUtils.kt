/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.utils

import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path


private val excludedPatterns =
    listOf(".*", "bin", "build", "node_modules", "target").map { FileSystems.getDefault().getPathMatcher("glob:$it") }

fun findSourceFiles(workspaceRoot: Path): Set<URI> {
    val sourceMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.{kt,kts}")
    return workspaceRoot.toFile()
        .walk()
//        .onEnter { isPathIncluded(it.toPath(), workspaceRoot) }
        .map { it.toPath() }
        .filter { sourceMatcher.matches(it) }
        .map { it.toUri() }
        .toSet()
}

fun isPathIncluded(file: Path, workspaceRoot: Path): Boolean = workspaceRoot.any { file.startsWith(it) }
        && excludedPatterns.none { pattern ->
    workspaceRoot
        .mapNotNull { if (file.startsWith(it)) it.relativize(file) else null }
        .flatten() // Extract path segments
        .any(pattern::matches)
}