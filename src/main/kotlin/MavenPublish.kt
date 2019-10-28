
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*


private val nexus_url = System.getenv().getOrDefault("NEXUS_URL", "https://nexus.spectrum.codes")
private val user_name = System.getenv().getOrDefault("NEXUS_USER", "developer")
private val user_password = System.getenv().getOrDefault("NEXUS_PASSWORD", "NO_NEXUS_PASSWORD")

fun Project.nexusDependencyRepository(){
    if(this.name==rootProject.name) {
        allprojects {
            repositories {
                mavenLocal()
                maven("$nexus_url/repository/maven-public") {
                    credentials {
                        username = user_name
                        password = user_password
                    }
                }
                mavenCentral()
                jcenter()

            }
        }
    }else{
        repositories {
            mavenLocal()
            maven("$nexus_url/repository/maven-public") {
                credentials {
                    username = user_name
                    password = user_password
                }

            }
            mavenCentral()
            jcenter()

        }
    }
}



fun Project.publishMaven(group_id: String = this.group.toString(), name: String = "") {
    var artefactName = name
    if(artefactName.isBlank()){
        artefactName = rootProject.name+"-"+this.name
        if(!artefactName.contains("spectrum")){
            artefactName = "spectrum-$artefactName"
        }
    }
    apply{
        plugin("org.gradle.maven-publish")
    }

    ensureDeployTask()

    with(extensions.getByType(PublishingExtension::class.java)){


        val release =project.properties.containsKey("release")
        val tag = project.properties.getOrDefault("tag","dev")!!.toString().replace("/","_")
        var publish_version = project.properties.getOrDefault("publish_version",project.version)!!.toString()
        var snapshot = false
        if(tag!="master"){
            snapshot = true

        }
        if(snapshot){
            publish_version+="-${tag}-SNAPSHOT"
        }
        var nexus_uri = "$nexus_url/repository/maven-releases/"
        if(snapshot){
            nexus_uri = "$nexus_url/repository/maven-snapshots/"
        }



        publications{

            repositories {
                maven {
                    // change to point to your repo, e.g. http://my.org/repo
                    url = uri(nexus_uri)
                    credentials{
                        username = user_name
                        password = user_password
                    }
                }
            }

            if (publications.findByName("maven") == null) {
                create<MavenPublication>("maven") {
                    groupId = group_id
                    artifactId = artefactName
                    version = publish_version
                    from(components["java"])
                }
            }


        }

        tasks {
            val deploy by existing
            val publish by existing
            if(release) {
                deploy {
                    dependsOn(publish)
                }
            }
            val publishMavenPublicationToMavenRepository by existing{
                doFirst{
                    val path = "${project.group}/${artefactName}".replace(".","/")+"/${publish_version}"
                    println("Publication will be: $nexus_url/repository/maven-public/$path/maven-metadata.xml")
                }
            }
        }
    }
}