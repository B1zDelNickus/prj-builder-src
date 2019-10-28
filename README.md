# Полноценный пример использования

История изменений: [смотерть тут](changelog.md)

Полноценный пример использования того, что описано ниже можно 
смотреть в [Пример проекта](https://gitlab.com/spectrum-internal/b2b-sell-reports)

Обратить внимание на 

1. [Настройки GIT](https://gitlab.com/spectrum-internal/b2b-sell-reports/blob/master/.gitmodules)
2. [Настройки GRADLE](https://gitlab.com/spectrum-internal/b2b-sell-reports/blob/master/build.gradle.kts)
3. [Настройки CI](https://gitlab.com/spectrum-internal/b2b-sell-reports/blob/master/.gitlab-ci.yml)


# Применение к проекту

Добавление как submodule в проект

```bash
git submodule add <relative_path>/buildSrc
```
> Внимание! Для совместимости в GitLab CI следует использовать только относительные пути 
например если у вас репозиторий https://gitlab.com/spectrum-internal/b2b-sell-reports, то добавлять
подмодуль надо как `git submodule add ../buildSrc`

После этого можно выполнить команду инициализации
```bash
./buildSrc/init
```
Которая настроит пустой проект GRADLE в режиме мультипроекта. 
> Внимание - команда безопасна и для повторного вызова - не затрагивает измененные и уже существующие файлы
> Внимание - пустой проект не добавляет ни одного модуля в проект, сами модули и их структуры формируются затем самим программистом.

После этого доступна задача `createmodule` чтобы создать модули вашего проекта (создаст дефолтные структуры директорий и зарегистрирует в `settings.gradle.kts`):
```bash
./gradlew createmodule -Pmodulename=commons,utils,extensions,bundle,integration
```

> Внимание - имена common(s) и bundle имеют особое назначение - `common` или `commons` будут назначены
**общей зависимостью** - все остальные модули будут от нее зависеть и наоборот
`bundle` - включает в себя все модули. Это прописывается в их `build.gradle.kts` и потом
вы можете подправить эту логику если надо


В `build.gradle.kts` вашего проекта требуется только плагин котлина и то только в случае если вы прямо в основном
билде хотите использовать расширения для компилятора Kotlin:
```yaml
plugins {
    kotlin("jvm")
}
```
При этом версию указывать не надо


# Использование

Данный buildSrc является набором утилит для настройки проекта.

Сейчас это `alpha` и заточена на использование в мультимодульных проектах.


## Основное расширение

```bash
fun spectrumMultimodule(version:String="1.0", grp:String=rootProjectName, body:Project.()->Unit)
```
Общая обертка для остальных расширений:
1. Настраивает правильную группу и версию проекта `prepareArtifact(...)` с установкой `codes.spectrum.`
2. Настраивает наш нексус в качестве основного репозитория `nexusDependencyRepository()`
3. Настраивает Kotlin+kotlintest(junit5) `standardKotlin()`
4. Устанавливает режим поддержки общих ресурсов `commonResources()`
5. Вызывает пользовательскую конфигурацию проекта `body()`
6. Для всех проектов с докерами настраивает задачу `deploy` `setupDockerDeploy()` настройка ведется с учетом  `-Prelease`
7. Для всех задач добавлят дополнительное логирование времени выполнения `logTaskDuration()`

## Поддержка публикации maven

В модуле, который должен публиковаться в nexus достаточно вызвать расширение `publishMaven()`
Настройка ведется с учетом опций `-Prelease`, `-Ptag`, `-Ppublish_version`

## Настройка сборки сервиса KTOR в докере

Достаточно выполнить `setupServices{Project.()->Boolean}` передав ламбду - признак, что модуль
является Ktor-сервисом.
В модуль будут установлены нужные зависимости и настроена публикация в докер,
с учетом `-Prelease`, `-Ptag`, `-Ppublish_version`

## Настройка общей завивисимости

Надо вызывать `commonDependency(Project)` передав ссылку на общий модуль
установит в качестве зависимости во все модули кроме указанного


# Обслуживание сценария деплоя в nexus и registry

1. Для запуска команд, связанных с деплоем недо вызывать `gradle deploy`, по умолчанию это только подготовка, а не сама запись
2. Чтобы был произведен физический пуш в registry и в nexus следует указывать `gradle deploy -Prelease`
3. Чтобы перекрыть базовую версию надо указать `-Ppublish_version=X.Y.Z`
4. Чтобы указать бранч сборки и дополнительный тег - следует использовать `-Ptag`

Если тег `-Ptag=master`, то публикация рассматривается как релизная
1. Версия докера становится просто `<timestamp>`
2. Версия мавен - базовая, указанная в `version` проекта или перекрыта через `-Ppublish_version`

Если тег отличается, то
1. Версия докера `${timestamp}-${tag}`
2. Версия мавен `${version|publish_version}-${tag}-SNAPSHOT`

# Настройки GITLAB-CI

## Переменные среды

|Переменная|Разъяснение|Значения|
|-----|-----|----|
|REGISTRY_TYPE|альяс реестра докеров|`gitlab`|`swarm`(умолчание)|
|REGISTRY_USER|имя пользователя в registry|обязательно, если используется докер|
|REGISTRY_PASSWORD|пароль пользователя в registry|обязательно, если используется докер|
|NEXUS_URL|URL нексуса|`https://nexus.swarm.su`(умолчание)|
|NEXUS_USER|имя пользователя на нексусе|`developer` по умолчанию|
|NEXUS_PASSWORD|пароль пользователя на нексусе|обязательно для gitlab.com, даже на чтение|

## Минимальный GITLAB-CI без шагов выполнения

Ниже показан шаблон GITLAB CI, который обеспечивает собственно
корректный чекаут и запуск CI, без собственно шагов:

```yaml

# Специальный образ, объединяющий alpine, docker, openjdk-8, gradle
# всегда надо использовать его
image: dpershin/gitlab-gradle-docker:5.1.1

# Обязательно - иначе не будет возможности кэшировать файлы .gradle 
# и сборки будут проходить крайне медленно
before_script:
  - export GRADLE_USER_HOME=`pwd`/.gradle
``` 

## Работа с данным buildSrc в качестве сабмодуля

Требуется указать CI что модули должны загружаться
```yaml
image: dpershin/gitlab-gradle-docker:5.1.1
before_script:
  - export GRADLE_USER_HOME=`pwd`/.gradle
variables:
  GIT_SUBMODULE_STRATEGY: normal
```

## Обеспечение использования DOCKER при сборке

Достаточно добавить сервис `dind`

```yaml
image: dpershin/gitlab-gradle-docker:5.1.1
before_script:
  - export GRADLE_USER_HOME=`pwd`/.gradle
services:
  - docker:dind
``` 

## Компиляция и тестирование для всех бранчей c кэшированием
```yaml
image: dpershin/gitlab-gradle-docker:5.1.1
before_script:
  - export GRADLE_USER_HOME=`pwd`/.gradle
stages:
  - test
# Основная сборочная фаза, с компиляцией и тестированием
test:
  stage: test
  script: gradle build
  cache:
    # Кэш привязываем к бранчу
    key: "$CI_COMMIT_REF_NAME"
    paths:
      # Внимание! В build.gradle.kts многомодульных проектов все проекты должны собираться в подпапки общего build (!)
      - build
      - .gradle
```

## Деплой докеров только для dev, master  с использованием ранее полученного кэша на чтение

Это по сути уже полный предлагаемый шаблон 

```yaml
image: dpershin/gitlab-gradle-docker:5.1.1
variables:
  GIT_SUBMODULE_STRATEGY: normal
services:
  - docker:dind
before_script:
  - export GRADLE_USER_HOME=`pwd`/.gradle
stages:
  - test
  - deploy
test:
  stage: test
  script: gradle build
  cache:
    # Кэш привязываем к бранчу
    key: "$CI_COMMIT_REF_NAME"
    paths:
      # Внимание! В build.gradle.kts многомодульных проектов все проекты должны собираться в подпапки общего build (!)
      - build
      - .gradle
       
# Фаза публикации докеров в репозитории, работает только для бранча master
# в явном виде заблокирована пере-сборка кода, кэш не всегда отрабаотывает
deploy:
  stage: deploy
  only:
    - master
    - dev
  cache:
    # Кэш привязываем к бранчу
    key: "$CI_COMMIT_REF_NAME"
    policy: pull
    paths:
      # Внимание! В build.gradle.kts многомодульных проектов все проекты должны собираться в подпапки общего build (!)
      - build
      - .gradle
  script:
    # опция --build-cache обязательна для оптимизации этой сборки
    - gradle deploy --build-cache -Prelease -Ptag=$CI_COMMIT_REF_NAME
```

## Зависимости по умолчанию

1. `serialization-json:common` - во все проекты Kotlin
2. `logging:bundle` - во все проекты Kotlin
3. `logging+rabbitmq` - в проекты сервисов (докеры)