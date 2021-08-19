/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.domain

data class SnippetsConfiguration(
    /** Whether code completion should return VSCode-style snippets. */
    var enabled: Boolean = false
)

data class CompletionConfiguration(
    val snippets: SnippetsConfiguration = SnippetsConfiguration()
)

data class LintingConfiguration(
    /** The time interval between subsequent lints in ms. */
    var debounceTime: Long = 200L
)

data class JVMConfiguration(
    /** Which JVM target the Kotlin compiler uses. See Compiler.jvmTargetFrom for possible values. */
    var target: String = "default"
)

data class CompilerConfiguration(
    val jvm: JVMConfiguration = JVMConfiguration()
)

data class IndexingConfiguration(
    /** Whether an index of global symbols should be built in the background. */
    var enabled: Boolean = false
)

data class ExternalSourcesConfiguration(
    /** Whether kls-URIs should be sent to the client to describe classes in JARs. */
    var useKlsScheme: Boolean = false,
    /** Whether external classes should be automatically converted to Kotlin. */
    var autoConvertToKotlin: Boolean = false
)

data class Configuration(
    val compiler: CompilerConfiguration = CompilerConfiguration(),
    val completion: CompletionConfiguration = CompletionConfiguration(),
    val linting: LintingConfiguration = LintingConfiguration(),
    var indexing: IndexingConfiguration = IndexingConfiguration(),
    val externalSources: ExternalSourcesConfiguration = ExternalSourcesConfiguration()
)
