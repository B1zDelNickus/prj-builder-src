import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

fun Project.commonResources() {
    subprojects {
        /*
        Наслоение ресурсов в дочерних проектах с перекрытием корневых
         */
        with(extensions.getByType(SourceSetContainer::class.java)) {
            getByName("main")
                .resources.setSrcDirs(listOf(
                "${rootProject.projectDir}/resources",
                "${projectDir}/src/main/resources",
                "${projectDir}/src/tmp/resources"
            ))
        }

        tasks.register("copyMarkDownToResources") {
            group = "documentation"
            doFirst {
                val files = rootProject.projectDir.listFiles().filter { it.name.endsWith(".md") || it.name.endsWith(".html") }
                if (files.isNotEmpty()) {
                    val dir = File(project.projectDir, "src/tmp/resources/doc").also { it.mkdirs() }
                    for (f in dir.listFiles()) {
                        f.delete()
                    }
                    for (f in files) {
                        f.copyTo(File(dir, f.name), true)
                    }
                }
            }
        }

        val copyJsonsAndYamls = tasks.register("copyRootJsonAndYamlToResources") {
            group = "resources"
            doFirst {
                val files = rootProject.projectDir.listFiles().filter { it.name.endsWith(".json") || it.name.endsWith(".yaml") }
                if (files.isNotEmpty()) {
                    val dir = File(project.projectDir, "src/tmp/resources").also { it.mkdirs() }
                    for (f in dir.listFiles()) {
                        f.delete()
                    }
                    for (f in files) {
                        f.copyTo(File(dir, f.name), true)
                    }
                }
            }
        }

        tasks.withType<KotlinCompile>{
            dependsOn(copyJsonsAndYamls)
        }

    }
}