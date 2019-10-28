import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.resolver.kotlinBuildScriptModelCorrelationId


fun DependencyHandlerScope.spectrumJson(withSchemas: Boolean = false, config: String = "api", version: String = "0.5-dev-SNAPSHOT") {
    config("codes.spectrum.serialization-json:spectrum-serialization-json-commons:$version")
    if (withSchemas) {
        config("codes.spectrum.serialization-json:spectrum-serialization-json-schemas:$version")
    }
}

fun DependencyHandlerScope.spectrumLogging(config: String = "implementation", version: String = "0.5-dev-SNAPSHOT") {
    config("codes.spectrum.logging:spectrum-logging-bundle:$version")
}

fun DependencyHandlerScope.rabbitMqAppender(config: String = "implementation", version: String = "2.1.7.RELEASE"){
    config("org.springframework.amqp:spring-rabbit:$version")
}


val apiCoreVersion =  "0.1-dev-SNAPSHOT"

fun DependencyHandlerScope.microCoreCommons(config: String = "api", version: String = apiCoreVersion){
    config("codes.spectrum.microcore:spectrum-microcore-commons:$version")
}

fun DependencyHandlerScope.microCore(config: String = "api", version: String = apiCoreVersion){
    config("codes.spectrum.microcore:spectrum-microcore-bundle:$version")
}


fun DependencyHandlerScope.apiCore(moduleName:String, config: String= "api", version: String =  apiCoreVersion){
    config("codes.spectrum.spectrum-api-core:spectrum-api-core-$moduleName:$version")
}


fun DependencyHandlerScope.archetypes(config: String = "api", version: String = apiCoreVersion) {
    apiCore("archetypes",config,version)
}

fun DependencyHandlerScope.legacyB2BModel(config: String = "api", version: String = apiCoreVersion) {
    apiCore("legacy-model",config,version)
}


fun DependencyHandlerScope.specgen(config: String = "implementation", version: String = apiCoreVersion) {
    apiCore("specgen",config,version)
}
fun DependencyHandlerScope.testCommons(config: String = "testImplementation", version: String = apiCoreVersion) {
    apiCore("test-commons",config,version)
}

fun DependencyHandlerScope.legacySpectrumUtils(config: String = "implementation", version: String = "19.05.28-SNAPSHOT"){
    config("codes.spectrum:spectrum-utils:$version")
}




fun DependencyHandlerScope.rabbitMqClient(config: String = "implementation", version: String = "5.7.1") {
    config("com.rabbitmq:amqp-client:$version")
}



fun Project.commonDependency(p:Project){
    subprojects{
        if(this!=p){
            dependencies{
                add("compile",p)
            }
        }
    }
}

fun Project.commonCompileOnlyDependency(p:Project){
    subprojects{
        if(this!=p){
            dependencies{
                add("compileOnly",p)
            }
        }
    }
}

fun Project.commonTestDependency(p:Project){
    subprojects{
        if(this!=p){
            dependencies{
                add("testCompile",p)
            }
        }
    }
}

val org.gradle.api.Project.`sourceSets`: org.gradle.api.tasks.SourceSetContainer get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer

fun Project.getTestOutput(name:String) = project(name).sourceSets.getByName("test").output
fun Project.useTestsFrom(name:String){
    dependencies{
        "testCompile"(getTestOutput(name))
    }
}