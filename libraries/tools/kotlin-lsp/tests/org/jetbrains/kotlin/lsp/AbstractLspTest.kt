/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import org.eclipse.lsp4j.launch.LSPLauncher
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.io.*
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
        val request = File(filePath).readText()
//        request = "Content-Length: ${request.length}\r\n\r\n"
//        "Content-Length: len\r\n\r\n"
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
        //todo save result with \r
        val result = readResponse().replace("\r", "")
        val expected = File(filePath).readText()
        assertEquals(expected, result, "Length of expected = ${expected.length}, result = ${result.length}")
    }

    private fun readResponse(): String {
        var result = ""
        // todo fix but some multi thread shit, maybe because of piped stream
        // wait at last read, but not get -1
        // flush to get all result

        while (lspOutput.available() > 0) {
            result += lspOutput.read().toChar()
            outStream.flush()
        }
        return result
    }
}