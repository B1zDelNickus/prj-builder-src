import org.gradle.api.*

fun Project.prepareArtifact(grp:String, ver:String = "1.0"){
    val resolvedVersion = this.properties.getOrDefault("publish_version",System.getenv().getOrDefault("PUBLISH_VERSION",ver))!!
    allprojects {
        group = grp
        version = resolvedVersion
    }
}