/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp

import com.intellij.lang.Language
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import java.net.URI

class SourceKtFile(uri: URI, factory: PsiFileFactoryImpl, text: String) {
    val language: Language?
    val ktFile: KtFile

    init {
        language = languageOf(uri)
        val shortName = uri.toString()
        val virtualFile = LightVirtualFile(shortName, KotlinLanguage.INSTANCE, StringUtilRt.convertLineSeparators(text))
        virtualFile.charset = CharsetToolkit.UTF8_CHARSET
        ktFile = factory.trySetupPsiForFile(virtualFile, KotlinLanguage.INSTANCE, true, false) as KtFile
    }

    constructor(uri: URI, factory: PsiFileFactoryImpl) : this(uri, factory, FileUtil.loadFile(
        File(uri),
        CharsetToolkit.UTF8,
        true
    ).trim { it <= ' ' })

    fun getDocument(): Document? {
        return FileDocumentManager.getInstance().getDocument(ktFile.virtualFile)
    }

    private fun languageOf(uri: URI): Language? {
        val fileName = uri.filePath?.fileName?.toString() ?: return null
        return when {
            fileName.endsWith(".kt") -> KotlinLanguage.INSTANCE
            else -> null
        }
    }

}
