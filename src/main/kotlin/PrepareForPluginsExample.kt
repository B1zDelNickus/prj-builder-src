import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create


open class GreetingPluginExtension {
    var message = "Hello from GreetingPlugin"
}

class GreetingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the 'greeting' extension object
        val extension = project.extensions.create<GreetingPluginExtension>("greeting")
        // Add a task that uses configuration from the extension object
        project.task("hello") {
            doLast {
                println(extension.message)
            }
        }
    }
}

