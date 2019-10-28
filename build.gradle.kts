
import java.time.LocalDate
import java.time.format.DateTimeFormatter



plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "1.2.3"
}
repositories {
    jcenter()
    mavenCentral()
}



dependencies {
    compile("com.bmuschko:gradle-docker-plugin:4.4.0")
    compile("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
    compile("com.google.code.gson:gson:2.8.5")
}

buildDir = File("../build/${name}")

val overwrite = properties.getOrDefault("overwrite","false").toString().toBoolean()
val overwrite_start = properties.getOrDefault("overwrite_start","false").toString().toBoolean()
val outerProjectName = File("..").canonicalFile.name
val outerPackage = "codes.spectrum.${outerProjectName.replace("-",".")}"

tasks.register("resetci") {
    this.doFirst {
        setup_ci()
        generate_scripts()
    }
}


tasks.register("updatelogback") {
    this.doFirst {
        val src =File("./resources/empty/resources/logback.xml")
        val trg =File("../resources/logback.xml")
        if(!trg.exists() || !trg.readText().startsWith("<!-- no-upgrade -->") ){
            trg.parentFile.mkdirs()
            src.copyTo(trg,true)
        }
    }
}

val withService =hasProperty("service") && properties.get("service").toString()=="true"

tasks.register("createproject"){
    this.doFirst{
        copy_wrappers()
        setup_ci()
        generate_gitignore()
        generate_root_kts()
        generate_settings()
        generate_options()
        generate_commons()
        if(withService) {
            generate_service()
        }
    }
}





fun setup_ci() {
    val ci = File("../.gitlab-ci.yml")
    if (!ci.exists() || !ci.readText().startsWith("# no-upgrade") ) {
        ci.writeText("""
# Специальный образ, объединяющий alpine, docker, openjdk-8/11, gradle
image: "${'$'}BUILD_IMAGE"

# Требуется для использования докера внутри сборки
services:
  - docker:dind

variables:
  GIT_SUBMODULE_STRATEGY: recursive

stages:
  - test
  - deploy



# Домашняя директория Gradle должна быть направлена именно на текущую .gradle проекта, до запуска
before_script:
  - echo "${'$'}{CI_REGISTRY_PASSWORD}" | docker login -u "${'$'}{CI_REGISTRY_USER}" --password-stdin ${'$'}{CI_REGISTRY}
  - export GRADLE_USER_HOME=`pwd`/.gradle

# Основная сборочная фаза, с компиляцией и тестированием
test:
  stage: test
  script: gradle build --refresh-dependencies
  cache:
    # Кэш привязываем к бранчу
    key: "${'$'}CI_COMMIT_REF_NAME"
    paths:
      # Внимание! В build.gradle.kts многомодульных проектов все проекты должны собираться в подпапки общего build (!)
      - build
      - .gradle



# Фаза публикации докеров в репозитории, работает только для бранча dev
# в явном виде заблокирована пере-сборка кода, кэш не всегда отрабаотывает
deploy-snapshot:
  stage: deploy
  only:
    - dev
    - master
  cache:
    # Кэш привязываем к бранчу
    key: "${'$'}CI_COMMIT_REF_NAME"
    policy: pull
    paths:
      # Внимание! В build.gradle.kts многомодульных проектов все проекты должны собираться в подпапки общего build (!)
      - build
      - .gradle
  script:
    # опция --build-cache обязательна для оптимизации этой сборки
    - gradle deploy --build-cache -Prelease -Ptag=dev --refresh-dependencies

# Фаза публикации докеров в репозитории, работает только для бранча master
# в явном виде заблокирована пере-сборка кода, кэш не всегда отрабаотывает
deploy-release:
  stage: deploy
  only:
    - master
  cache:
    # Кэш привязываем к бранчу
    key: "${'$'}CI_COMMIT_REF_NAME"
    policy: pull
    paths:
      # Внимание! В build.gradle.kts многомодульных проектов все проекты должны собираться в подпапки общего build (!)
      - build
      - .gradle
  script:
    # опция --build-cache обязательна для оптимизации этой сборки
    - gradle deploy --build-cache -Prelease -Ptag=master --refresh-dependencies
        """)
    }
}

fun copy_wrappers(){
    if(!File("../gradlew").exists() || overwrite){
        File("../gradle/wrapper").mkdirs()
        File("gradlew").copyTo(File("../gradlew"),true)
        File("gradlew.bat").copyTo(File("../gradlew.bat"),true)
        File("gradle/wrapper/gradle-wrapper.jar").copyTo(File("../gradle/wrapper/gradle-wrapper.jar"),true)
        File("gradle/wrapper/gradle-wrapper.properties").copyTo(File("../gradle/wrapper/gradle-wrapper.properties"),true)
    }
}

fun generate_gitignore(){
    val gitignore = File("../.gitignore")
    if(!gitignore.exists()||overwrite){
        gitignore.writeText("""
# Created by .ignore support plugin (hsz.mobi)
### Gradle template
.gradle
**/build/

# Ignore Gradle GUI config
gradle-app.setting

# Avoid ignoring Gradle wrapper jar file (.jar files are usually ignored)
!gradle-wrapper.jar

# Cache of project
.gradletasknamecache

# # Work around https://youtrack.jetbrains.com/issue/IDEA-116898
# gradle/wrapper/gradle-wrapper.properties

# IDEA
.idea
*.iml
*.log
/*.hprof

**/tmp
.kotlintest
        """)
    }
}

fun generate_service(){
    build_proj_struct("service")
    val startFile = File("../service/src/main/kotlin/codes/spectrum/${outerProjectName.replace("-","/")}/Start.kt")
    if(!startFile.exists()||overwrite){
        startFile.writer().use{
            it.appendln("""

package ${outerPackage}
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty


fun main() {
    embeddedServer(Netty
        , 8080
        , module = module)
        .start(wait = true)
}
            """)
        }
    }

    val moduleFile = File("../service/src/main/kotlin/codes/spectrum/${outerProjectName.replace("-","/")}/Module.kt")
    if(!moduleFile.exists()||overwrite_start){
        moduleFile.writer().use{
            it.appendln("""

package ${outerPackage}

import io.ktor.application.Application


val module: Application.() -> Unit = {
    // insert server logic here
}
            """)
        }
    }
}



fun generate_commons(){
    build_proj_struct("commons")
}

fun generate_scripts() {
    File("../upbuilder").writeText("""#!/bin/bash
cp ./buildSrc/upbuilder ./buildSrc/upbuilder-tmp
cd ./buildSrc
./upbuilder-tmp
cd ..
rm -rf ./buildSrc/upbuilder-tmp""")
}

fun generate_options(){
    val root = File("../${name}")
    val options = File(root.canonicalPath + "/gradle.options")
    if (!options.exists() || overwrite) {
        options.writer().use {
            it.appendln("kapt.incremental.apt=true")
        }
    }
}

fun build_proj_struct(name:String, body:()->String = {""}){
    val root = File("../${name}")
    val main = File("../${name}/src/main/kotlin/codes/spectrum/${outerProjectName.replace("-","/")}")
    val test = File("../${name}/src/test/kotlin/codes/spectrum/${outerProjectName.replace("-","/")}")
    val main_dir_holder = File(main.canonicalPath+"/.gitkeep")
    val test_dir_holder = File(test.canonicalPath+"/.gitkeep")
    main.mkdirs()
    test.mkdirs()
    if(main.listFiles().isEmpty()) {
        main_dir_holder.writeText("")
    }
    if(test.listFiles().isEmpty()) {
        test_dir_holder.writeText("")
    }
    val settings = File(root.canonicalPath+"/build.gradle.kts")
    if(!settings.exists()||overwrite){
        settings.writer().use{
            it.appendln(body())
        }
    }



}

fun generate_settings() {
    val settingsKts = File("../settings.gradle.kts")
    if (!settingsKts.exists() || overwrite) {
        val formatter = DateTimeFormatter.ofPattern("yy.MM.dd")
        val includes = mutableListOf("commons")
        if(withService){
            includes.add("service")
        }
        settingsKts.writer().use {
            it.appendln("""
rootProject.name = "$outerProjectName"
include(${includes.map{"\"${it}\""}.joinToString(", ")})
                """)
        }
    }
}

fun generate_root_kts() {
    val rootKts = File("../build.gradle.kts")
    if (!rootKts.exists() || overwrite) {
        val formatter = DateTimeFormatter.ofPattern("yy.MM.dd")

        rootKts.writer().use {
            if(withService) {
                it.appendln("""
spectrumMultimodule("${formatter.format(LocalDate.now())}") {
    //Устанавливаем проект :commons
    project(":commons").let {
        //что она - источник для публикации в maven
        it.publishMaven()
        //в качестве зависимости по умолчанию для всех остальных
        commonDependency(it)
        //что все остальное - сервисы кроме нее
        setupServices { this.name != it }
    }
}
                """)
            }else{
                it.appendln("""
spectrumMultimodule("${formatter.format(LocalDate.now())}") {
    //Устанавливаем проект :commons
    project(":commons").let {
        //что она - источник для публикации в maven
        it.publishMaven()
        //в качестве зависимости по умолчанию для всех остальных
        commonDependency(it)
        //что все остальное - сервисы кроме нее

    }
    project(":service").let{
        setupAsService()
    }
}
                """)

            }
        }
    }
}