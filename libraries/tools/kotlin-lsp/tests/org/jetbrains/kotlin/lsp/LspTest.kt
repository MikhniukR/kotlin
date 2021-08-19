/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class LspTest {

    class InitializeTest : AbstractLspTest() {

        @Test
        fun initializesSimpleProject() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/initialize.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/initialize.out")
        }

        @Test
        fun initializeTestReference() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testReference/initialize.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testReference/initialize.out")
        }
    }

    class SimpleProjectTest : AbstractLspTest(
        "libraries/tools/kotlin-lsp/testData/requests/simpleProject/initialize.in",
        "libraries/tools/kotlin-lsp/testData/requests/simpleProject/initialize.out"
    ) {

        @Test
        fun documentSymbol() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/documentSymbol.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/documentSymbol.out")
        }

        @Test
        fun goToDefinitionSameFile() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/goToDefinitionSameFile.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/goToDefinitionSameFile.out")
        }

        @Test
        fun goToDefinitionOtherFile() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/goToDefinitionOtherFile.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/goToDefinitionOtherFile.out")
        }

        @Disabled
        @Test
        fun goToDefinitionStdlibPrint() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/goToDefinitionStdlibPrint.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/goToDefinitionStdlibPrint.out")
        }

        @Test
        fun hoverConstructor() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/hoverConstructor.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/hoverConstructor.out")
        }

        @Test
        fun hoverVoidFunction() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/hoverVoidFunction.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/hoverVoidFunction.out")
        }

        @Test
        fun hoverNonVoidFunction() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/hoverNonVoidFunction.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/hoverNonVoidFunction.out")
        }

        @Test
        fun hoverGeneric() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/hoverGeneric.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/hoverGeneric.out")
        }

        @Test
        fun referenceVariable() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/referenceVariable.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/referenceVariable.out")
        }

        @Test
        fun referenceConstructor() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/referenceConstructor.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/referenceConstructor.out")
        }

        @Test
        fun referenceConstructorParameter() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/referenceConstructorParameter.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/referenceConstructorParameter.out")
        }

        @Test
        fun referenceClassVariable() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/referenceClassVariable.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/referenceClassVariable.out")
        }

        @Test
        fun referenceOtherFile() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/referenceOtherFile.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/referenceOtherFile.out")
        }

        @Test
        fun workspaceSymbol() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/workspaceSymbol.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/workspaceSymbol.out")
        }
    }

    class ReferenceTest : AbstractLspTest(
        "libraries/tools/kotlin-lsp/testData/requests/testReference/initialize.in",
        "libraries/tools/kotlin-lsp/testData/requests/testReference/initialize.out"
    ) {

        @Test
        fun referenceFunction() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testReference/referenceFunction.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testReference/referenceFunction.out")
        }

        @Test
        fun referenceFunctionWithParameter() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testReference/referenceFunctionWithParameter.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testReference/referenceFunctionWithParameter.out")
        }

        @Test
        fun referenceConstructor() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testReference/referenceConstructor.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testReference/referenceConstructor.out")
        }
    }

}