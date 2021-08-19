/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp.utils

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

private var threadCount = 0

class AsyncExecutor {
    private val workerThread = Executors.newSingleThreadExecutor { Thread(it, "async${threadCount++}") }

    fun execute(task: () -> Unit) =
        CompletableFuture.runAsync(Runnable(task), workerThread)

    fun <R> compute(task: () -> R) =
        CompletableFuture.supplyAsync(Supplier(task), workerThread)

    fun <R> computeOr(defaultValue: R, task: () -> R?) =
        CompletableFuture.supplyAsync(Supplier {
            try {
                task() ?: defaultValue
            } catch (e: Exception) {
                defaultValue
            }
        }, workerThread)

    fun shutdown(awaitTermination: Boolean) {
        workerThread.shutdown()
        if (awaitTermination) {
            workerThread.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
        }
    }
}
