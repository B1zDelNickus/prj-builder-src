import org.gradle.api.Project
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.TaskProvider
import java.io.File

import org.gradle.kotlin.dsl.*

fun Project.externalPrecompile() {
    val externalDir = File(this.projectDir, "external")
    if (externalDir.exists() && externalDir.listFiles().any { it.isDirectory }) {
        var subtasks = mutableListOf<TaskProvider<*>>()
        for (ext in externalDir.listFiles().filter { it.isDirectory }) {
            subtasks.add(tasks.register<GradleBuild>("ex-${ext.name}") {
                group = "externals"
                dir = ext
                tasks = listOf("publishToMavenLocal")
            })
        }
        val buildExternals = tasks.register("buildExternals") {
            group = "externals"
            dependsOn(*subtasks.toTypedArray())
        }
        gradle.projectsEvaluated {
            subprojects {
                tasks {
                    val compileKotlin by existing
                    compileKotlin {
                        dependsOn(buildExternals)
                    }
                }
            }
        }

    }
}