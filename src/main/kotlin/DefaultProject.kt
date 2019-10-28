import org.gradle.api.Project



fun Project.spectrumMultimodule(
    ver: String = "",
    grp: String = rootProject.name,
    body: Project.() -> Unit) {
    ensureProperty("spectrum-version", "19.05.28-SNAPSHOT")
    ensureProperty("gson-version", "2.8.5")
    val fail_on_first_module = ensureProperty("fail-on-first-module", "false").toBoolean()
    ensureProperty("package-name", rootProject.name.replace("-", "."))
    ensureProperty("multi-service", false)
    prepareArtifact("codes.spectrum.$grp", ver)

    //Перенаправляем зависимости на чтение на nexus.spectrum.codes
    nexusDependencyRepository()
//Стандартная настройка Kotlin-проекта
    standardKotlin()
// Устанавливаем режим наложения ресурсов от рутового на терминальные
    commonResources()

    body()

    if(!fail_on_first_module) {

        val checkTests = tasks.register("check-all-tests-fail") {
            group = "verification"
            dependsOn(subprojects.map { it.tasks.named("test") })
            doLast {
                val fail_module_count = project.rootProject.getInt(test_fail_count_extra)
                if (fail_module_count > 0) {
                    throw Exception("There are ${fail_module_count} modules with test errors")
                }
            }
        }

        subprojects.forEach {
            it.tasks.named("check") {
                dependsOn(checkTests)
            }
        }

        tasks.register("build") {
            dependsOn(checkTests)
        }

        tasks.register("cleanTest") {
            group = "verification"
            dependsOn(subprojects.map { it.tasks.named("clean") })
        }

        tasks.register("test") {
            group = "verification"
            dependsOn(checkTests)
        }

    }



// Настраиваем докер-деплой для настроенных в setupServices сервисов
    setupDockerDeploy()
// Выводим по требованию дениса продолжительность выполняемых задач
    logTaskDuration()

    externalPrecompile()

    projectCreateModuleTask()
}
