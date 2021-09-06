/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import org.eclipse.lsp4j.launch.LSPLauncher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.Executors

abstract class AbstractLspTest() {
    private val inStream = PipedInputStream()
    private val outStream = PipedOutputStream()
    private val lspInput = PipedOutputStream()
    private val lspOutput = PipedInputStream()

    private var sendInit = false
    private var initializeIn = ""
    private var initializeOut = ""

    constructor(initializeIn: String, initializeOut: String) : this() {
        sendInit = true
        this.initializeIn = initializeIn
        this.initializeOut = initializeOut
    }

    init {
        inStream.connect(lspInput)
        lspOutput.connect(outStream)
    }

    @BeforeEach
    fun initServer() {
        val server = KotlinLanguageServer()
        val threads = Executors.newSingleThreadExecutor { Thread(it, "client") }
        val launcher = LSPLauncher.createServerLauncher(server, inStream, outStream, threads) { it }

        server.connect(launcher.remoteProxy)
        launcher.startListening()

        if (sendInit) {
            sendRequest(initializeIn)
            checkResult(initializeOut)
        }
    }

    fun sendRequest(filePath: String) {
        var request = File(filePath).readText()
        request = replaceFilePathForAbsolute(request)
        request = "Content-Length: ${request.length}\r\n\r\n" + request
        lspInput.write(request.toByteArray())
        lspInput.flush()
    }

    fun checkResult(filePath: String) {
        var wait = 0
        while (lspOutput.available() == 0) {
            Thread.sleep(100)
            wait += 100
            outStream.flush()

            assertFalse(wait > 10_000, "Wait for response more than 10 seconds")
        }
        val result = readResponse()
        var expected = File(filePath).readText()
        expected = replaceFilePathForAbsolute(expected)
        expected = "Content-Length: ${expected.length}\r\n\r\n" + expected
        assertEquals(expected, result, "Length of expected = ${expected.length}, result = ${result.length}")
    }

    private fun readResponse(): String {
        var result = ""

        while (lspOutput.available() > 0) {
            result += lspOutput.read().toChar()
            outStream.flush()
        }
        return result
    }

    private fun replaceFilePathForAbsolute(request: String): String =
        request.replace(Regex("\"uri\" *: *\"[a-zA-Z/:.-]*\"")) { toAbsoluteFilePath(it) }

    private fun toAbsoluteFilePath(match: MatchResult): String =
        "\"uri\":\"file://${File(match.value.split("\"")[3]).absolutePath}\""

}