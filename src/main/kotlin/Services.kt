import com.bmuschko.gradle.docker.DockerExtension
import com.bmuschko.gradle.docker.DockerJavaApplication
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaApplication
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.*

fun DependencyHandlerScope.compile(ref: Any) = add("compile", ref)

fun Project.setupAsService() {
    val self = this
    apply {
        plugin("com.bmuschko.docker-java-application")
        plugin("org.gradle.application")
    }
    setupKtor()

    dependencies{
        spectrumLogging()
        rabbitMqAppender()
    }

    val release: Boolean = properties.containsKey("release")
    var explicittag = properties.getOrDefault("tag", "").toString().replace("/", "_")
    if (release && explicittag == "master") {
        explicittag = ""
    }
    val format = SimpleDateFormat("yyyy-MM-dd-HH-mm")
    var dockertag = if (release) {
        if (explicittag.isEmpty()) {
            format.format(Date())
        } else {
            format.format(Date()) + "-" + explicittag
        }
    } else {
        if (explicittag.isEmpty()) {
            "latest"
        } else {
            explicittag
        }
    }
    dockertag = this.ensureProperty("dockertag", dockertag)


    val registryType = System.getenv().getOrDefault("REGISTRY_TYPE", "swarm")

    val registryAddress = when (registryType) {
        "gitlab" -> "registry.gitlab.com"
        "swarm" -> "registry.spectrum.codes"
        else -> error("not supported registry type $registryType")
    }

    val regitstryRoot = when (registryType) {
        "gitlab" -> "spectrum-internal/${rootProject.name}"
        else -> rootProject.name
    }

    val mainClass = with(extensions.getByType(JavaApplication::class.java)) {
        if(findProperty("main-class")!=null){
            mainClassName = findProperty("main-class")!!.toString()
        }else {
            val hotreload: Boolean = properties.getOrDefault("hotreload", "false").toString().toBoolean()
            val parentName = project.parent!!.name.replace("-", ".")
            if (parentName == "b2b.sell.reports") {
                //LEGACY  !!!
                mainClassName = "codes.spectrum.${project.parent!!.name.replace("-", ".")}.${name.replace("-", "")}.StartKt"
            } else if (ensureProperty<Boolean>("multi-service")) {
                mainClassName = "codes.spectrum.${ensureProperty("package-name")}.${project.name}.StartKt"
            } else {
                mainClassName = "codes.spectrum.${ensureProperty("package-name")}.StartKt"
            }
            if (hotreload) {
                applicationDefaultJvmArgs = listOf("-Dhotreload=true")
            }
        }
        mainClassName
    }

    with(extensions.getByType(DockerExtension::class.java)) {

        with((this as ExtensionAware).extensions.getByType(DockerJavaApplication::class.java)) {
            baseImage.set("openjdk:11.0.3-jdk-slim-stretch")
            ports.set(listOf(8080))
            tag.set("$registryAddress/$regitstryRoot/$name:$dockertag")
            exec {
                clear()
                instructions.addAll(
                        Dockerfile.EnvironmentVariableInstruction(
                            mutableMapOf(
                                "JAVA_OPTS" to "",
                                "SERVICE_NAME" to "${rootProject.name}-${self.name}"
                            )
                        ),
                        object : Dockerfile.Instruction {
                            override fun getKeyword(): String? = "ENTRYPOINT"
                            override fun getText(): String? = "ENTRYPOINT java \$JAVA_OPTS -cp /app/resources:/app/classes:/app/libs/* $mainClass"
                        })
            }
        }
    }


    tasks.withType<DockerPushImage> {
        val docker_user = System.getenv().getOrDefault("REGISTRY_USER_${registryType.toUpperCase()}", "NO_USER")
        val docker_pass = System.getenv().getOrDefault("REGISTRY_KEY_${registryType.toUpperCase()}", "NO_KEY")
        registryCredentials.apply {
            url.set("https://" + registryAddress)
            username.set(docker_user)
            password.set(docker_pass)
        }
    }

    tasks.withType<KotlinCompile> {
        dependsOn("copyMarkDownToResources")
    }

    this.setupDockerDeploy()

}

val ktor_version = "1.2.1"
fun Project.setupKtor() {
    val ktor_version = "1.2.1"
    dependencies {
        compile("io.ktor:ktor-server-core:$ktor_version")
        compile("io.ktor:ktor-server-netty:$ktor_version")
        compile("io.ktor:ktor-gson:$ktor_version")
        compile("io.ktor:ktor-html-builder:$ktor_version")
        compile("io.ktor:ktor-auth:$ktor_version")
        compile("io.ktor:ktor-client-apache:$ktor_version")
        compile("io.ktor:ktor-client-logging:$ktor_version")
        compile("ch.qos.logback:logback-classic:1.2.3")
        compile("com.atlassian.commonmark:commonmark:0.12.1")
        compile("com.atlassian.commonmark:commonmark-ext-gfm-tables:0.12.1")
    }
}

fun Project.setupServices(filter: Project.() -> Boolean = { false }) {
    subprojects {
        if (this.filter()) {
            this.setupAsService()
        }
    }
}