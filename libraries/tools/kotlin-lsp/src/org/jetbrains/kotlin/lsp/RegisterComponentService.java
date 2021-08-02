/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lsp;

import com.intellij.mock.MockProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.fir.low.level.api.FirIdeResolveStateService;

public class RegisterComponentService {

    public static void registerFirIdeResolveStateService(@NotNull MockProject project) {
        project.getPicoContainer().registerComponentInstance(FirIdeResolveStateService.class.getName(), new FirIdeResolveStateService(project));
    }
}
