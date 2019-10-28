package sources

import com.google.gson.GsonBuilder
import commonDependency
import commonTestDependency
import ensurePackage
import ensureProperty
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import publishMaven
import setupAsService
import spectrumMultimodule
import java.io.File
import java.time.Instant


fun Project.sourceProject(ver:String = "",body: Project.() -> Unit = {}){


    val sourceDef = setupSourceDefinition()
    setupResetTemplateTask()
    if (sourceDef.code.isNotBlank()) {
        spectrumMultimodule(ver, "sources.${sourceDef.packageName}") {
            sourceDependencies(sourceDef)
            sourcesDeployments(sourceDef)
            setupSourceInitializationTasks(sourceDef)
            setupSourceUpgradeTask(sourceDef)
        }
        body()
    }
}

private fun Project.sourcesDeployments(sourceDef: SourceDefinition) {
    if (sourceDef.librarySet) {
        subprojects {
            publishMaven()
        }
    } else {
        project(":model") {
            publishMaven()
        }
        project(":client") {
            publishMaven()
        }
        project(":provider") {
            publishMaven()
        }
        project(":service") {
            setupAsService()
        }
        project(":agent") {
            setupAsService()
        }
    }
}

private fun Project.sourceDependencies(sourceDef: SourceDefinition) {
    val model = project(":model") {
        dependencies {
            add("compile", "com.google.code.gson:gson:${ensureProperty("gson-version")}")
            add("compile", "codes.spectrum.serialization-json:spectrum-serialization-json-commons:0.5-dev-SNAPSHOT")
            add("compile", "codes.spectrum.serialization-json:spectrum-serialization-json-schemas:0.5-dev-SNAPSHOT")
            add("compile", "codes.spectrum:konveyor:0.1.11")
            "implementation"("com.atlassian.commonmark:commonmark:0.13.0")
        }
    }
    val test = project(":test")
    if (sourceDef.code != "core") {
        subprojects {
            val self = this
            if (!self.name.startsWith("ex_")) {
                dependencies {
                    add("compile", "codes.spectrum.sources.core:spectrum-core-${self.name}:${ensureProperty("source-core-version", "0.5.0-dev-SNAPSHOT")}")
                }
            }
        }
    }

    commonDependency(model)
    commonTestDependency(test)


    val client = project(":client"){
        val ktor_version = "1.2.1"
        dependencies{
            add("compile","io.ktor:ktor-client-core:$ktor_version")
            add("compile","io.ktor:ktor-client-apache:$ktor_version")
            add("compile","io.ktor:ktor-client-logging:$ktor_version")
        }
    }

    val service = project(":service"){
        dependencies{
            add("compile",project(":provider"))
        }
    }

    val agent = project(":agent"){
        dependencies{
            val spectrum_legacy_version = property("spectrum-version") as String
            add("compile",project(":transport"))
            "implementation"("codes.spectrum:spectrum-amqp:$spectrum_legacy_version")
            "implementation"("codes.spectrum.bus:bus-core-lib:$spectrum_legacy_version")
        }
    }

}

private fun Project.setupSourceDefinition(): SourceDefinition {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val sourceDefFile = File(rootProject.projectDir, "source.json")
    var sourceDef = SourceDefinition()
    if (sourceDefFile.exists()) {
        sourceDef = gson.fromJson(sourceDefFile.readText(), SourceDefinition::class.java)
    }

    ensureProperty("multi-service", true)
    if (sourceDef.code.isNotBlank()) {
        ensureProperty("package-name", "sources." + sourceDef.code.replace("-", "_"))
    }else{
        ensureProperty("package-name", "sources")
    }

    return ensureProperty("sourceDef", sourceDef)

}

private fun Project.getNormalizedName(): String = rootProject.projectDir.name.replace("-", "_")

private fun replaceDemoInPackage(content: String, code: String): String {
    val packageRegex = """package\s*.*\s+""".toRegex()
    val packageString = packageRegex.find(content)?.value
    val newPackageString = packageString?.replace("""\.demo\b""".toRegex(), ".${code}") ?: ""
    return if (packageString != null)
        content.replace(packageString, newPackageString)
    else
        content
}

private fun addToScript(file: File, text: String, afterRegex: Regex? = null, toStartElseToEnd: Boolean = true) {
    if (!file.exists())
        return
    val bashRegex = """#!/bin/(sh|bash)\s*""".toRegex()
    val content = file.readText()
    if (content.contains(text))
        return
    val shell = bashRegex.find(content)?.groupValues?.get(1) ?: return
    val contentAfterShell = content.substringAfter(bashRegex.find(content)?.value ?: "")
    val newContent = "#!/bin/$shell\n${
        if (afterRegex != null && afterRegex.containsMatchIn(contentAfterShell)) {
            val afterString = afterRegex.find(contentAfterShell)!!.value
            contentAfterShell.replace(afterString, "$afterString$text\n")
        }
        else {
            if (toStartElseToEnd)
                "$text\n$contentAfterShell"
            else
                "$contentAfterShell\n$text"
        }
    }"
    file.writeText(newContent)
}

private fun replaceOrAddInScript(file: File, regex: Regex, text: String, afterRegex: Regex? = null, toStartElseToEnd: Boolean = true) {
    if (!file.exists())
        return
    val content = file.readText()
    if (!regex.containsMatchIn(content))
        addToScript(file, text, afterRegex, toStartElseToEnd)
    else
        file.writeText(content.replace(regex, text))
}

private fun checkPreCommit(projectName: String, insomniaExportTime: String = Instant.now().toString()) {
    val preCommit = File(".githooks/pre-commit")

    addToScript(preCommit, "export GENERATE_SOURCE_ARTEFACTS=true")
    addToScript(preCommit, "./gradlew :gen:cleanTest :gen:test --tests codes.spectrum.${projectName}.gen.*", """export\s*GENERATE_SOURCE_ARTEFACTS\s*=\s*\w*\s*""".toRegex())
    replaceOrAddInScript(
        preCommit,
        """export\sINSOMNIA_EXPORT_TIME\s*=\s*"(.*)"""".toRegex(),
        "export INSOMNIA_EXPORT_TIME=\"${insomniaExportTime}\"",
        """export\s*GENERATE_SOURCE_ARTEFACTS\s*=\s*\w*\s*""".toRegex()
    )
    addToScript(preCommit, "git add insomnia.json", toStartElseToEnd = false)
    addToScript(preCommit, "git add insomnia.yaml", toStartElseToEnd = false)
    addToScript(preCommit, "git add service/src/main/resources/ui.html", toStartElseToEnd = false)
}

// Функция заменяет в полученной строке demoCode и demoClassName
// на полученные code и className
fun replaceDemo(string: String, code: String, className: String): String {
    var content = string
    if (code.isNotBlank())
        content = content.replace("""\.demo\b""".toRegex(), ".$code")
    if (className.isNotBlank())
        content = content.replace("""\b(I?)Demo""".toRegex(), "$1$className")
    return content
}

// Функция рекурсивно проходит по файлам и модифицирует содержимое
fun replaceRecursively(file: File, transformer: (String) -> String) {
    if (file.isDirectory)
        file.listFiles().forEach { replaceRecursively(it, transformer) }
    else {
        file.writeText(transformer(file.readText()))
    }
}

// Функция находит в text первое выражение startRegex,
// затем после этого выражения с учетом вложенности
// ищет блок между скобками startChar - endChar и возвращает его
fun getBetweenBrackets(text: String, startRegex: Regex, startChar: Char = '(', endChar: Char = ')'): String {
    var innerLevel = 0
    var start = -1
    var end = -1
    val textAfter = text.substringAfter(startRegex.find(text)?.value ?: "")

    textAfter.forEachIndexed { i, c ->
        if (start != -1 && end != -1)
            return@forEachIndexed

        if (c == startChar)
            innerLevel++

        if (c == endChar)
            innerLevel--

        if (innerLevel == 1 && start == -1)
            start = i

        if (innerLevel == 0 && start != -1)
            end = i
    }

    return textAfter.substring(start, end + 1)
}

// Функция находит в text первое выражение startRegex,
// затем после этого выражения с учетом вложенности
// ищет блок между скобками startChar - endChar,
// для него применяет метод body и заменяет в text
// содержимое блока на результат работы body
fun replaceBetweenBrackets(text: String, startRegex: Regex, startChar: Char = '(', endChar: Char = ')', body: (String) -> String): String {
    val between = getBetweenBrackets(text, startRegex, startChar, endChar)
    return text.replace(between, body(between))
}

private fun checkSourceJson(sourceDef: SourceDefinition? = null, forceReplace: Boolean = false) {
    val code = sourceDef?.packageName ?: "demo"
    val prefix = sourceDef?.classNamePrefix ?: "Demo"
    val name = sourceDef?.name ?: "demoname"

    val sourceJson = File("./source.json")
    if (!sourceJson.exists() || sourceJson.readText().isBlank() || forceReplace)
        sourceJson.writeText(
"""{
    "code":"$code",
    "name":"$name",
    "className": "$prefix",
    "sourceClazzName": "codes.spectrum.sources.$code.provider.${prefix}Source",
    "contextClazzName": "codes.spectrum.sources.$code.${prefix}Context",
    "requestClazzName": "codes.spectrum.sources.$code.model.${prefix}Request",
    "resultClazzName": "codes.spectrum.sources.$code.model.${prefix}Result",
    "queryClazzName": "codes.spectrum.sources.$code.model.${prefix}Query"
}"""
        )
    else
        sourceJson.writeText(replaceDemo(sourceJson.readText(), code, prefix))
}

private fun Project.setupResetTemplateTask(){
    fun forceCleanDirectory(dir: File) {
        for (f in dir.listFiles()) {
            if (f.isDirectory) {
                forceCleanDirectory(f)
            } else {
                f.delete()
            }
        }
        dir.delete()
    }

    val reset_template = tasks.register("reset-template") {
        group = "sources"
        doFirst {
            checkSourceJson()
            File("./README.md").writeText(File("./buildSrc/src/main/kotlin/sources/README.md").readText())
            File("./SourceDescription.md").writeText(File("./buildSrc/src/main/kotlin/sources/SourceDescription.md").readText())

            for (d in arrayOf("test", "model", "provider", "client", "service", "db", "transport", "agent")) {
                val dir = File("./${d}")
                if (dir.exists()) {
                    forceCleanDirectory(dir)
                    dir.mkdirs()
                }
            }

            File("./gen/src/test/kotlin/codes/spectrum/${getNormalizedName()}/gen/GenerateSourceArtefactsTest.kt").delete()
            File("./gen/src/test/resources/includes").delete()

            val buildSrcPattern = "/buildSrc/resources/sources/"

            fun copyFromResource(dir:File){
                for (f in dir.listFiles()) {
                    if (f.isDirectory) {
                        copyFromResource(f)
                    } else {
                        val path = f.canonicalPath.replace("\\", "/")
                        println(path)
                        val module = path.substringAfter(buildSrcPattern).substringBefore("/")
                        val newpath = if (module != "gen")
                            path.replace(buildSrcPattern, "/")
                        else
                            path.replace(buildSrcPattern, "/").replace("demo", getNormalizedName())
                        val newfile = File(newpath)
                        newfile.parentFile.mkdirs()
                        println(newpath)
                        val content = f.readText()
                        val newContent = if (module == "gen") {
                            replaceDemoInPackage(content, getNormalizedName())
                        }
                        else
                            content
                        newfile.writeText(newContent)
                    }
                }
            }
            copyFromResource(File("${rootProject.projectDir}/buildSrc/resources/sources"))

            checkPreCommit(getNormalizedName())
        }
    }
}

private fun replaceInsomniaExportTime(string: String, value: String? = null) =
    string.replace("""export\sINSOMNIA_EXPORT_TIME="(.*)"""".toRegex()) {
        "export INSOMNIA_EXPORT_TIME=\"${value ?: Instant.now()}\""
    }

private fun Project.setupSourceInitializationTasks(sourceDef:SourceDefinition){


    val checkSourceInitialized = tasks.register("check-source-initialized") {
        group = "sources"
        doFirst {
            if (rootProject.name != "source") {
                if (sourceDef.code.isBlank()) {
                    throw Exception("Source not defined")
                }
            }
        }
    }

    fun visitDir(dir: File) {
        for (f in dir.listFiles()) {
            if (f.isDirectory) {
                if (f.name == "demo") {
                    val result = File(f.parentFile, sourceDef.packageName.replace(".","/"))
                    f.renameTo(result)
                    visitDir(result)
                } else {
                    visitDir(f)
                }
            } else {
                var content = f.readText()
                if (f.extension == "html") {
                    content = content.replace("""\bdemo\b""".toRegex(), sourceDef.packageName)
                    content = content.replace("""\bdemoname\b""".toRegex(), sourceDef.name)
                    content = content.replace("""\b(I?)Demo""".toRegex(), "$1${sourceDef.classNamePrefix}")
                }
                else {
                    content = content.replace("""\.demo\b""".toRegex(), ".${sourceDef.packageName}")
                    content = content.replace("""\b(I?)Demo""".toRegex(), "$1${sourceDef.classNamePrefix}")
                }
                val filename = f.canonicalPath.replace("\\","/").replace("""\b(I?)Demo""".toRegex(), "$1${sourceDef.classNamePrefix}")
                f.writeText(content)
                f.renameTo(File(filename))
            }
        }
    }

    val model = project(":model")
    val test = project(":test")
    val setupSource = tasks.register("setup-source") {
        group = "sources"
        dependsOn(checkSourceInitialized)
        if (sourceDef.code != "") {
            doFirst {
                subprojects {
                    visitDir(File(this.projectDir,"src"))
                }
                checkSourceJson(sourceDef, true)

                regenerateStructureAndCommonTemplates(sourceDef, model, test)
            }
        }
    }
    subprojects {
        tasks.withType<KotlinCompile>{
            dependsOn(checkSourceInitialized)
        }
    }
}

private fun Project.regenerateStructureAndCommonTemplates(sourceDef: SourceDefinition, model: Project, test: Project) {
    subprojects {
        if (this.name != "gen") {
            this.ensurePackage(sourceDef.packageName(this))
            if (this != model && this != test) {
                model.ensurePackage(sourceDef.packageName(this))
            }
        }
    }
    fun processTemplateDir(dir: File) {
        if (dir.exists()) {
            for (f in dir.listFiles()) {
                if (f.isDirectory) {
                    processTemplateDir(f)
                } else {
                    var newFileName = f.canonicalPath.replace("\\", "/").replace("/templates/", "/")
                    if (newFileName.contains("/.sources/")) {
                        val srcCode = """/\.sources/(?<code>[^/]+)""".toRegex().find(newFileName)!!.groups["code"]!!.value
                        if (srcCode != sourceDef.code) {
                            continue
                        } else {
                            newFileName = newFileName.replace("""/.source/${srcCode}/""", "/")
                        }
                    }
                    newFileName = newFileName.replace("/PACKAGE/", "/${sourceDef.packageName}/")
                    newFileName = newFileName.replace("/MAIN/", "/src/main/kotlin/codes/spectrum/sources/${sourceDef.packageName}/")
                    newFileName = newFileName.replace("/MAINRES/", "/src/main/resources/")
                    newFileName = newFileName.replace("/TEST/", "/src/test/kotlin/codes/spectrum/sources/${sourceDef.packageName}/")
                    newFileName = newFileName.replace("/TESTRES/", "/src/test/resources/")
                    newFileName = newFileName.replace("/CLS", "/${sourceDef.classNamePrefix}")
                    val newFile = File(newFileName)
                    newFile.parentFile.mkdirs()
                    newFile.writeText(f.readText())
                    sourceDef.interpolate(newFile)
                }
            }
        }
    }
    processTemplateDir(File(rootProject.projectDir, "templates"))
}

private fun Project.setupSourceUpgradeTask(sourceDef: SourceDefinition) {

    // Функция копирует файлы из src в dst
    // и заменяет в содержимом demoCode и demoClassName
    fun copyFile(src: File, dst: File, code: String = "", className: String = "") {
        src.copyRecursively(dst)
        replaceRecursively(dst) {
            replaceDemo(it, code, className)
        }
    }

    // Функция проверки наличия файла в проекте,
    // при отстутсвии копирует его из demoFile
    // и при необходимости трансформирует через transformer
    fun check(
        code: String = "",
        className: String = "",
        file: File,
        demoFile: File,
        ifOnlyNotExists: Boolean = true,
        transformer: (String) -> String = fun (text: String) = text) {
        if (!ifOnlyNotExists || !file.exists() || file.isDirectory != demoFile.isDirectory) {
            try { copyFile(demoFile, file, code, className) } catch (_: Throwable) { /* File exists */ }
            replaceRecursively(file, transformer)
        }
    }

    // Функция проверки наличия определенного import
    // при отсутствии import он добавляется в результирующую строку
    fun checkAndAddImport(text: String, importString: String): String {
        val packageRegex = """package\s*.*\s+""".toRegex()
        val importRegex = """(?:import\s*.*\s+)+""".toRegex()
        val imports = importRegex.find(text)?.value?.trimEnd() ?: ""
        val packageString = packageRegex.find(text)?.value ?: ""

        val newImports = if (!imports.contains(importString))
            "$imports\n$importString"
        else
            imports

        return if (imports.isBlank())
            text.replace(packageString, "$packageString$newImports\n")
        else
            text.replace(imports, newImports)
    }

    // Функция проверяет необходимые implementation
    // в model/build.gradle.kts
    fun checkImplementation() {
        val buildGradleKts = File("./model/build.gradle.kts")
        var content = buildGradleKts.readText()

        val dependenciesRegex = """dependencies\s*""".toRegex()
        val implementCommonMark = "    implementation(\"com.atlassian.commonmark:commonmark:0.13.0\")"

        content = if (dependenciesRegex.containsMatchIn(content))
            replaceBetweenBrackets(content, dependenciesRegex, '{', '}') { between ->
                var newBetween = between.substringBeforeLast("}")
                if (!between.contains(implementCommonMark.trimIndent()))
                    newBetween += "$implementCommonMark\n"
                "$newBetween}"
            }
        else
            "$content${ if (content.isNotBlank()) "\n\n" else "" }dependencies {\n$implementCommonMark\n}"

        buildGradleKts.writeText(content)
    }

    // Функция проверяет в Request классе наличия параметров
    // debug и caseCode, а также возвращает название параметра
    // DebugMode - на случай если DebugMode уже был написан,
    // но с другим названием
    fun checkRequestAndGetDebugParamName(code: String, className: String): String {
        val requestClass = File("./model/src/main/kotlin/codes/spectrum/sources/$code/model/${className}Request.kt")
        var content = requestClass.readText()

        val paramsRegex = """class\s*${className}Request\s*""".toRegex()
        val debugParamRegex = """(\w*)\s*:\s*DebugMode""".toRegex()
        val sourceQueryRegex = """:\s*SourceQuery\s*<\s*${className}Query\s*>\s*""".toRegex()

        val params = content.substringAfter(paramsRegex.find(content)?.value ?: "")

        var debugParamName = "debug"

        content = replaceBetweenBrackets(content, paramsRegex) { between ->
            var newBetween = between.substringBeforeLast(")").trimEnd()
            if (!between.contains("DebugMode"))
                newBetween += ",\n    debug: DebugMode? = null"
            else
                debugParamName = debugParamRegex.find(params)?.groupValues?.get(1) ?: ""

            if (!between.contains("caseCode"))
                newBetween += ",\n    val caseCode: String = \"\""

            "$newBetween\n)"
        }

        content = replaceBetweenBrackets(content, sourceQueryRegex) { between ->
            if (!"debug\\s*=\\s*\\w*".toRegex().containsMatchIn(between))
                between.substringBeforeLast(")") + ", debug = $debugParamName)"
            else
                between
        }

        content = checkAndAddImport(content, "import codes.spectrum.sources.DebugMode")

        requestClass.writeText(content)
        return debugParamName
    }

    // Функция проверяет наследование IQuery классом Query
    fun checkQuery(code: String, className: String) {
        val queryClass = File("./model/src/main/kotlin/codes/spectrum/sources/$code/model/${className}Query.kt")
        var content = queryClass.readText()

        val queryRegex = """class\s*${className}Query\s*""".toRegex()
        val iQueryRegex = """\)\s*:\s*IQuery\s*""".toRegex()
        content = replaceBetweenBrackets(content, queryRegex) { between ->
            if (!iQueryRegex.containsMatchIn(content))
                "$between: IQuery "
            else
                between
        }

        content = checkAndAddImport(content, "import codes.spectrum.sources.core.model.IQuery")
        queryClass.writeText(content)
    }

    tasks.register("source-upgrade") {
        group = "sources"
        doFirst {
            val code = sourceDef.packageName
            val className = sourceDef.className

            checkSourceJson(sourceDef, true)
            checkImplementation()
            val debugName = checkRequestAndGetDebugParamName(code, className)
            checkQuery(code, className)
            checkPreCommit(getNormalizedName())

            check(
                code = code,
                className = className,
                file = File("./gen/src/test/kotlin/codes/spectrum/${getNormalizedName()}/gen"),
                demoFile = File("./buildSrc/resources/sources/gen/src/test/kotlin/codes/spectrum/demo/gen"),
                ifOnlyNotExists = false
            ) {
                replaceDemoInPackage(it, getNormalizedName())
            }
            check(
                code = code,
                className = className,
                file = File("./gen/src/test/resources/includes/"),
                demoFile = File("./buildSrc/resources/sources/gen/src/test/resources/includes/")
            )
            check(
                code = code,
                className = className,
                file = File("./model/src/main/kotlin/codes/spectrum/sources/$code/source"),
                demoFile = File("./buildSrc/resources/sources/model/src/main/kotlin/codes/spectrum/sources/demo/source")
            )
            check(
                file = File("./SourceDescription.md"),
                demoFile = File("./buildSrc/src/main/kotlin/sources/SourceDescription.md")
            )
            check(
                code = code,
                className = className,
                file = File("./provider/src/test/kotlin/codes/spectrum/sources/$code/provider/TestCasesTest.kt"),
                demoFile = File("./buildSrc/resources/sources/provider/src/test/kotlin/codes/spectrum/sources/demo/provider/TestCasesTest.kt"),
                ifOnlyNotExists = false
            ) {
                it.replace("debug = case.debug,", "$debugName = case.debug,")
            }
        }
    }
}