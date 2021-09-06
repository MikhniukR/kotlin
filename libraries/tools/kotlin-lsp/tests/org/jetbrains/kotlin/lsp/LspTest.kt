/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

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

        @Test
        fun initializesTestHover() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testHover/initialize.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testHover/initialize.out")
        }

        @Test
        fun initializeTestEdit() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.out")
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
        fun hoverStdlibPrint() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/simpleProject/hoverStdlibPrint.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/simpleProject/hoverStdlibPrint.out")
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

        @Test
        fun referenceNoMain() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testReference/referenceNoMain.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testReference/referenceNoMain.out")
        }
    }

    class HoverTest : AbstractLspTest(
        "libraries/tools/kotlin-lsp/testData/requests/testHover/initialize.in",
        "libraries/tools/kotlin-lsp/testData/requests/testHover/initialize.out"
    ) {

        @Test
        fun hoverListOf() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testHover/hoverListOf.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testHover/hoverListOf.out")
        }

        @Test
        fun hoverFunctionList() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testHover/hoverFunctionList.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testHover/hoverFunctionList.out")
        }
    }

    class EditTest : AbstractLspTest() {

        @Test
        fun addLineAndHover() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.out")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChange.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/hover.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/hover.out")
        }

        @Test
        fun addLineAndDefinition() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.out")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChange.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinition.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinition.out")
        }

        @Test
        fun addLineAndReference() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.out")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChange.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/reference.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/reference.out")
        }

        @Test
        fun removeLinesAndHover() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.out")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChangeRemove.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/hoverRemove.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/hoverRemove.out")
        }

        @Test
        fun addLineTwiceAndHover() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.out")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChange.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChange.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/hoverTwiceChange.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/hoverTwiceChange.out")
        }

        @Test
        fun addFunctionAndCallInOtherFileWithTypes() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.out")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChangeSecond.in")

            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChangeFirstNoArgs.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionFirstNoArgs.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionFirstNoArgs.out")

            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChangeFirstInt.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionInt.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionInt.out")

            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChangeFirstString.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionString.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionString.out")
        }

        @Test
        fun addCallAndFunctionsInOtherFileWithTypes() {
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/initialize.out")

            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChangeFirstNoArgs.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChangeSecondNoArgs.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionFirstNoArgs2.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionFirstNoArgs2.out")

            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChangeFirstInt.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChangeSecondInt.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionInt2.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionInt2.out")

            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChangeFirstString.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/didChangeSecondString.in")
            sendRequest("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionString2.in")
            checkResult("libraries/tools/kotlin-lsp/testData/requests/testEdit/goToDefinitionString2.out")
        }
    }

}