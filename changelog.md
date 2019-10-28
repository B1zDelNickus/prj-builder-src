# История изменений



## 2019-10-24
1. upbuilder - добавил создание модуля `gen`
1. `task: copyRootJsonToResources` переименовал в `task: copyRootJsonAndYamlToResources` и внес добавление файлов расширением `yaml` в ресурсы
1. @comdiv: upgrade Kotlin to 1.3.50
1. @comdiv: upgrade KotlinTest to 3.4.2

## 2019-10-11 
1. Реализация отсроченной проверки всех тестов после проверки всех подмодулей (не валится больше на первом же тесте)


## 2019-10-04
1. Работа по задаче [S-9235](https://youtrack.spectrum.codes/issue/S-9235):
    1. Обновлены тесты генераторов артефактов источника (теперь это один тест)
    1. Обновлен тест кейсов
    1. Обновлен `SourceDescriptorInstance`
    1. Обнволены таски `source-upgrade` и `reset-template`

## 2019-10-02
1. Работа по задаче [S-9235](https://youtrack.spectrum.codes/issue/S-9235):
    1. Создание модуля `gen` при вызове `init`
    1. Создание `pre-commit` хука, вызывающего тесты из модуля `gen`
    1. Изменения повлекли обновление тасков `sources: reset-template`, `sources: source-update`

## 2019-09-25
1. Работа по задаче [S-9235](https://youtrack.spectrum.codes/issue/S-9235):
   1. Перенесены генераторы ui.html и Insomnia из задач [S-9009](https://youtrack.spectrum.codes/issue/S-9009) и [S-9181](https://youtrack.spectrum.codes/issue/S-9181)
   1. Перенесен тест для кейсов `TestCases` в `provider:test`
   1. Перенесены объекты стандартных REST-площадок (`DevRestSystem`, `ProdRestSystem`, `LocalRestSystem`, `LocalRelativeRestSystem`)
   1. Перенесен `pre-commit git hook`
   1. Классы `DemoRequest`, `DemoResult`, `DemoContext` вынесены из `IDemoSource` в отдельные классы
   1. sourceProject - добавлена замена `demo` параметров в `doctemplate.html`, `ui.html`, а также в папке `.githooks`
   1. dependency - добавлена зависимость [`com.atlassian.commonmark`](https://github.com/atlassian/commonmark-java) для `model`
   1. Добавлена задача для проектов источников `Task (sources): source-update` - обеспечивает миграцию старых версий шаблонов на новый (пока добавлена миграция генераторов ui.html и Insomnia)

## 2019-07-16
1. dependency - более унифицированы зависимости spectrum-api-core
2. dependency - добавлена возможность установки от тестов других проектов (имеются в виду тестовые утилиты) `useTestFrom(":PROJECTNAME")`


## 2019-07-08
1. upbuilder - введена поддержка хранимых общих гит-хуков, их можно добавлять в ./.githooks
2. depdendency - `legacyB2BModel()` - для подключения старых моделей хранения B2B

## 2019-07-05
1. dependency - добавил legacySpectrumUtils - для подключения текущей версии старых spectrum-utils

## 2019-07-03
1. dependency - добавлена зависимость `archetypes` - для aрхетипических интерфейсов spectrum-api
2. dependency - добавлена зависимость `specgen` - для расширений для генерации спек

## 2019-06-28
1. createmodule - поддержка имени `service` - оформляет его как `setupAsService()`
2. createmodule - поддержка маски `*-service` - оформляет как сервис и делает зависимость от `:*`
3. createmodule - добавляет README.md в каждый модуль

## 2019-06-22
1. createmodule - не создает специального пакета для common(s), .gitkeep в директориях, множественное создание модулей (modulename - много имен через запятую)
2. dependency - добавлен `rabbitMqClient()` - нативный Java клиент RabbitMQ, `microCoreCommons`, `microCore` для зависимостей на microcore 

## 2019-06-21 
1. maven - изменена логика присвоения имен артефактам - теперь включают в себя spectrum и имя корневого проекта
2. dependency - исправлены неправильные циркулярные зависимости библиотек logging
3. Новая задача для корневых проектов `createmodule` - `./gradlew createmodule -Pmodulename=xxx` - создает пустые структуры модулей и прописывает в `settings.gradle.kts` 

## 2019-06-19 

1. logback.xml - основные поля в RabbitMQ аппендер определены через MDC
2. upbuilder - будет обновлять логбэк в общих ресурсах если в начале файла не прописать `<!-- no-upgrade -->`
3. upbuilder - не будет обновлять CI если в начале `# no-upgrade`
4. upbuilder - исправлена ошибка, которая не позволяла апгрейдить сам файл upbuilder при вызове (требует сначала ручного запуска pull для buildSrc)

## 2019-06-18 (сборный)

1. Kotlin ^ 1.3.31
2. Gradle ^ 5.4.1
2. CI - `BUILD_IMAGE` - для перекрытия образа для билда
3. CI - принудительный `--refresh-dependencies`
4. Добавлен шаблон и init для пустых проектов
5. CommonDependency/Kotlin - автоматическое добавление serialization-json
6. CommonDependency/Kotlin - автоматическое добавление logging + rabbit (для сервисов)
7. Kotlin - поддержка директории для автосгенерированных сорцов `${buildDir}/generated/src/kotlin`
8. Kotlin - `-Pfulltestout=true` включает режим полного вывода STD с логов (при необходимости сложной отладки на CI например)
9. Maven - локальный мавен выставлен старшим приоритетом
10. Docker - `-Pdockertag` - полное принудительное присвоение тега докера
11. Docker - `ENV SERVICE_NAME` - для имени приложения в логах
12. source - `codes.spectrum:konveyor` добавлен в основные зависимости 
13. upbuilder - поддерживает аргумент nocommit для грейда без создания коммита




## 2019-06-02

1. Исправление [S-7192](https://youtrack.spectrum.codes/issue/S-7192) - при запуске в пустом проекте `./buildSrc/upbuilder` не создавались врапперы для gradle
2. Фича [S-7193](https://youtrack.spectrum.codes/issue/S-7192) - добавлен скрипт `./buildSrc/init` для инициализации новых проектов

