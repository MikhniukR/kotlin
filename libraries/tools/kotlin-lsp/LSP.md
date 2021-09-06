## About LSP

Данная страница подразумевает что вы уже прочитали [краткий обзор протокола]((https://microsoft.github.io/language-server-protocol/overviews/lsp/overview/))

**LS** - Language Server. Сервер, который предоставляет функциональные возможности для инструментов поддерживающих протокол.

**LSP** - Language Server Protocol. Протокол стандартизирующий взаимодействия сервера и клиента.

Основная идея LSP - не писать реализации всех функций для каждого языка и каждого инструмента заново, 
выполняя _N_(языков)*_M_ (инструментов) работы, а написать один раз LS для языка и client для инструмента, сведя это к _N_+_M_ работы.

Протокол поддерживает как запросы, требующие ответа, так и уведомления на которые не нужно отвечать.
Запросы и уведомления могут отправляться в обе стороны.

Все запросы делятся на несколько типов:
* [General Message](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#initialize). Включающий в себя запросы для старта сервера, завершения его работы и логирования
* [Workspace](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#workspace_workspaceFolders) для работы c проектами. Включающий в себя добавление проекта, изменение проекта и другие запросы на уровне проекта
* [Text synchronization](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#textDocument_synchronization) для синхронизации текста на сервере с текстом на клиенте
* [Language features](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#textDocument_completion) включающий в себя функциональные запросы к серверу, такие как go to definition, find usages, type at point и другие 
* [Client](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#client_registerCapability) Для изменения возможностей клиента

## Настройка возможностей

Поскольку не все сервера поддерживают все запросы/возможности протокола, существуют настройки возможностей(Capabilities) для клиента и сервера.
Которые передаются в запросе [initialize](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#initialize) и описываются отдельно для каждого запроса.

## Language features

Для большинства функциональных запросов приходит файл и позиция в нем. [Specification](https://microsoft.github.io/language-server-protocol/specifications/specification-3-17/#textDocumentPositionParams)
В качестве идентификатора файла приходит его URI [Specification](https://microsoft.github.io/language-server-protocol/specifications/specification-3-17/#uri) вида `"uri":"file:///Users/Roman.Mikhniuk/work/kotlin/libraries/tools/kotlin-lsp/testData/projects/simpleProject/src/test.kt"`.
Для идентификации позиции приходит line + character, которые потом преобразуются в _offset_ и обратно.
