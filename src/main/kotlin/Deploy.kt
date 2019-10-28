import org.gradle.api.Project
import org.gradle.kotlin.dsl.existing
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.*

fun Project.ensureDeployTask(){
    if(tasks.findByName("deploy")==null){
        tasks.register("deploy")
    }
}

/**
 * Setup deploy for terminal
 */
fun Project.setupDockerDeploy() {
    if(this==rootProject) {
        subprojects {
            this.setupDockerDeploy()
        }
    }else{
        ensureDeployTask()
        tasks {
            val release: Boolean = properties.containsKey("release")
            val deploy by existing
            val dockerPushImage = tasks.findByName("dockerPushImage")
            val dockerBuildImage = tasks.findByName("dockerBuildImage")
            deploy {
                if (release) {
                    if (null != dockerPushImage) {
                        dependsOn(dockerPushImage)
                    }
                } else {
                    if (null != dockerBuildImage) {
                        dependsOn(dockerBuildImage)
                    }
                }
            }
        }
    }
}