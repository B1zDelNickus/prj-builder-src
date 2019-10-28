import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

val test_fail_count_extra = "testfailcount"

fun Project.getInt(name: String) = if (this.extra.has(name)) {
    this.extra.get(name) as Int
} else 0

fun Project.inc(name: String) {

    val current = if (project.rootProject.extra.has(name)) {
        project.rootProject.extra.get(name) as Int
    } else {
        0
    }
    project.rootProject.extra.set(name, current + 1)
}

fun Project.standardKotlin() {

    subprojects {
        apply {
            plugin("org.jetbrains.kotlin.jvm")
            //Example of apply custom plugin
            plugin(GreetingPlugin::class.java)
        }
        //Очень значимо для кэша грэдла, чтобы весь build был в одной папке
        buildDir = File("${rootProject.buildDir}/${this.name}")
        dependencies {
            compile(kotlin("stdlib-jdk8","1.3.50"))
            compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")

            // Корневые наши утилиты для JSON не зависят от нашего логинга
            if(rootProject.name=="logging" || rootProject.name=="serialization-json") {
                compile("ch.qos.logback:logback-classic:1.2.3")
            }else{
                spectrumLogging()
            }
            // А вот от JSON все кроме него самого зависит
            if(rootProject.name!="serialization-json"){
                spectrumJson()
            }
            add("testImplementation", "io.kotlintest:kotlintest-runner-junit5:3.4.2")
        }


        with(extensions.getByType(SourceSetContainer::class.java)) {
            getByName("main").allSource.apply {
                srcDir("${this@subprojects.buildDir}/generated/src/kotlin")
            }
            getByName("main").java.apply {
                srcDir("${this@subprojects.buildDir}/generated/src/java")
            }
        }

        val needFullTestOutput = ensureProperty("fulltestout",System.getenv("FULL_TEST_OUT")?:"false").toBoolean()
        tasks.withType<Test> {
            useJUnitPlatform()
            val fail_on_first_module = ensureProperty("fail-on-first-module", "false").toBoolean()
            if(!fail_on_first_module) {
                ignoreFailures = true
            }

            testLogging {
                if(needFullTestOutput) {
                    this.showStandardStreams = true
                    this.showExceptions = true
                    this.showStackTraces = true
                }
            }

            if(!fail_on_first_module) {
                addTestListener(object : TestListener {
                    override fun beforeSuite(suite: TestDescriptor) {}
                    override fun beforeTest(testDescriptor: TestDescriptor) {}
                    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
                    override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                        if (suite.getParent() == null && 0L != result.failedTestCount) {
                            project.rootProject.inc(test_fail_count_extra)

                        }
                    }
                })
            }
        }




        allprojects {
            tasks.withType<KotlinCompile>().all {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
        }
    }
}