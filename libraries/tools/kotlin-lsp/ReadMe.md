# Kotlin Language Server
Это репозиторий с прототипом реализации Language Server Protocol для Kotlin с использованием FIR-компилятора.


Полезные ссылки:

* [Краткое описание LSP](https://microsoft.github.io/language-server-protocol/overviews/lsp/overview/)
* [Немного более подробное описание LSP](https://github.com/MikhniukR/kotlin/blob/lsp/libraries/tools/kotlin-lsp/LSP.md)
* [Полная спецификация LSP](https://microsoft.github.io/language-server-protocol/specifications/specification-current/)
* [Реализация внешнего пользователя со старым компилятором](https://github.com/fwcd/kotlin-language-server)
* [Презентация в конце стажировки(private video)](https://youtu.be/EjCGpHiuzes?t=1147)
* [Atom package](https://github.com/fwcd/atom-ide-kotlin) для использования LS. [Run in Atom](https://github.com/MikhniukR/kotlin/tree/lsp/libraries/tools/kotlin-lsp#run-in-atom)
* [VS Code plugin](https://github.com/fwcd/vscode-kotlin) для использования LS. [Run in VS Code](https://github.com/MikhniukR/kotlin/tree/lsp/libraries/tools/kotlin-lsp#run-in-vs-code)
* [Красивое описание функций с примерами](https://scalameta.org/metals/docs/)

## Build
* Убедиться что у вас собирается проект *Kotlin*
* Установить JDK 11
* Заменить путь к stdlib в [build.gradle.kts](https://github.com/MikhniukR/kotlin/blob/lsp/libraries/tools/kotlin-lsp/build.gradle.kts#L55) для корректной сборки
* Заменить пути к stdlib в [ServiceManager](https://github.com/MikhniukR/kotlin/blob/lsp/libraries/tools/kotlin-lsp/src/org/jetbrains/kotlin/lsp/ServiceManager.kt#L61) и [FirModuleResolveStateConfiguratorImpl](https://github.com/MikhniukR/kotlin/blob/lsp/libraries/tools/kotlin-lsp/src/org/jetbrains/kotlin/lsp/implementations/FirModuleResolveStateConfiguratorImpl.kt#L143) для поддержки stdlib в LS

## Working functions
На данный момент реализованы:
* Editing support с помощью `Document.insertString`
* Type at point (Hover in LSP)
* Go to Definition
* Find usages
* Global/Document symbol

## About realisation
Для упрощения написания реализации LSP была использована библиотека [eclipse LSP4J](https://github.com/eclipse/lsp4j) 
согласно [гайду](https://github.com/eclipse/lsp4j/blob/master/documentation/README.md).

При запуске сервера ему блокируется чтение файлов вне workspace, поэтому обычные логеры(slf4, log4j) зависают. 
Можно логировать втупую добавляя строки в файл, но ничего не читая.

Тесты реализованы как явная отправка запроса и сравнение ответа. 
Для их стандартизации из них убран полный путь к файлу, которые проставляется позже.
Так же убраны заголовки, что бы была возможность редактировать запрос.

## Top things to add
* [Completion](https://microsoft.github.io/language-server-protocol/specifications/specification-3-17/#textDocument_completion)
* [Rename](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#textDocument_rename)
* [Signature help (aka. parameter hints)](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#textDocument_signatureHelp)
* Работа с файлами rename/add/delete/move.
  В плагине для VS Code и Atom нет поддержки [запросов](https://microsoft.github.io/language-server-protocol/specifications/specification-3-17/#workspace_willCreateFiles) для работы с файлами.
  Это реализовано через [workspaceEdit](https://microsoft.github.io/language-server-protocol/specifications/specification-3-17/#workspaceEdit)
* Поддержка нескольких workspaces одним сервером. Уже заложена в [протоколе](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#workspace_workspaceFolders).
* Поддержка библиотек. Поскольку LSP более общий протокол, то в него явно не заложен путь передачи структуры проекта и этот момент нужно продумать.
* Поддержка прогресса выполнения запросов [progress](https://microsoft.github.io/language-server-protocol/specifications/specification-3-17/#workDoneProgress)


## Run in VS Code
В vs-code установить плагин [Kotlin](https://github.com/fwcd/vscode-kotlin) by fwcd

Выполнить для сборки исполняемого файла
```bash
gradlew clean kotlin-lsp:installShadowDist
```

В настройках плагина(Settings -> Extensions -> Kotlin) в параметр Language Server Path прописать путь к исполняемому файлу (например `/Users/Username/work/kotlin/libraries/tools/kotlin-lsp/build/install/kotlin-lsp-shadow/bin/kotlin-lsp`)

HotKeys для использования функций LSP:
* Type at point (Hover in LSP) - навести на функцию
* Go to Definition - _F12_ или _cmd/cntr + click_
* Find usages - _shift + F12_
* Document symbol - _cmd + shift + O_
* Global symbol - _cmd + T_

## Run in Atom
В Atom установить package [ide-kotlin]((https://github.com/fwcd/atom-ide-kotlin)) by fwcd

Выполнить для сборки исполняемого файла
```bash
gradlew clean kotlin-lsp:installShadowDist
```

Поскольку в Atom нет настройки для кастомного пути к Language Server, то нужно словить процесс Language Server и подменить искомый файл.
Нужно копировать всю папку `kotlin-lsp-shadow` поскольку `lib` тоже нужны.

Подменять нужно будет примерно такой файл `~/.atom/packages/ide-kotlin/install/server/bin/kotlin-language-server`.

Удобно делать вот так(из `kotlin/libraries/tools/kotlin-lsp/build/install`): 
```bash
cp -r kotlin-lsp-shadow/ ~/.atom/packages/ide-kotlin/install/server/
cd ~/.atom/packages/ide-kotlin/install/server/bin/
mv mv kotlin-language-server kotlin-language-server2
mv kotlin-lsp kotlin-language-server
```

HotKeys для использования функций LSP:
* Type at point (Hover in LSP) - навести на функцию
* Go to Definition - _cmd/cntr + click_
* Find usages - _cmd + option + shift + F_