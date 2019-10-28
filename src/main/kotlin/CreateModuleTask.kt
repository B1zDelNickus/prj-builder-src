import org.gradle.api.Project
import java.io.File

fun Project.projectCreateModuleTask() {
	val project = this
    tasks.register("createmodule") {
        doFirst {
            val modulename = this.project.property("modulename")!!.toString()
            if (modulename.contains(",")) {
				val modules = modulename.split(",").map { it.trim() }
                for (m in modules) {
                    doCreateModule(m, project, modules)
					if(m.endsWith("-service")){
						doCreateModule(m.replace("-service",""), project, modules)
					}
					
                }
            } else {
                doCreateModule(modulename, project, listOf(modulename))
            }
        }
    }
}

private fun doCreateModule(modulename: String, project: Project, modules:List<String>) {
    if (modulename.isBlank()) throw Exception("No module name given")
    val moduleRoot = File(project.rootProject.rootDir, "$modulename")
    if (moduleRoot.exists() && !moduleRoot.isDirectory) throw Exception("File ${moduleRoot} already exists and is not directory")
    moduleRoot.mkdirs()
	var packageDir = (if (modulename == "commons" || modulename == "common") "${project.rootProject.name.replace("-", "_")}" else "${project.rootProject.name.replace("-", "_")}/${modulename.replace("-", "_")}")
	if(modulename.endsWith("-service")){
	   packageDir = packageDir.replace("_service\$".toRegex(),"/service")
	}
    if(modulename!="bundle") {
        File(moduleRoot, "src/main/kotlin/codes/spectrum/$packageDir").mkdirs()
        File(moduleRoot, "src/main/kotlin/codes/spectrum/$packageDir/.gitkeep").createNewFile()
        File(moduleRoot, "src/main/resources").mkdirs()
        File(moduleRoot, "src/main/resources/.gitkeep").createNewFile()
        File(moduleRoot, "src/test/kotlin/codes/spectrum/$packageDir").mkdirs()
        File(moduleRoot, "src/test/kotlin/codes/spectrum/$packageDir/.gitkeep").createNewFile()
        File(moduleRoot, "src/test/resources").mkdirs()
        File(moduleRoot, "src/test/resources/.gitkeep").createNewFile()
    }
	if(modulename!="commons" && modulename!="bundle" && (modules.contains("commons")||File(project.rootProject.rootDir,"commons").exists())){
		File(project.rootProject.rootDir,"commons/src/main/kotlin/codes/spectrum/$packageDir").mkdirs()			
		File(project.rootProject.rootDir,"commons/src/main/kotlin/codes/spectrum/$packageDir/.gitkeep").createNewFile()
		File(project.rootProject.rootDir,"commons/src/test/kotlin/codes/spectrum/$packageDir").mkdirs()			
		File(project.rootProject.rootDir,"commons/src/test/kotlin/codes/spectrum/$packageDir/.gitkeep").createNewFile()
	}
	val readmefile = File(moduleRoot,"README.md")
	if(!readmefile.exists()){
	    readmefile.writeText("""
# Документация по модулю `${project.rootProject.name}/${modulename}`	
""")
	}
    val gradlefile = File(moduleRoot, "build.gradle.kts")
    if (!gradlefile.exists()) {
       gradlefile.createNewFile()
        if(modulename=="bundle"){
            gradlefile.writeText("""
val self = this.project
dependencies{
   for(p in rootProject.subprojects){
      if(p.name!=self.name && p.name!="service" && !p.name.endsWith("-service")){
        api(p)
      }
   }
}
            """)
        }else if(modulename=="common"|| modulename=="commons"){
            gradlefile.writeText("""
val self = this.project
rootProject.subprojects{
    if(this.name!=self.name){
        dependencies{
            api(self)
        }
    }
}
            """)
        }else if(modulename=="service"){
			gradlefile.writeText("""
import org.gradle.kotlin.dsl.extra
this.extra.set("main-class","codes.spectrum.${packageDir.replace("/",".")}.StartKt")
setupAsService()

						""")
		
		}else if(modulename.endsWith("-service")){
			gradlefile.writeText("""
import org.gradle.kotlin.dsl.extra
this.extra.set("main-class","codes.spectrum.${packageDir.replace("/",".")}.StartKt")
dependencies{
   api(project(":${modulename.replace("-service","")}"))
}
setupAsService()

						""")
		
		}
	}

    if(modulename=="service"||modulename.endsWith("-service")){
        val main =  File(moduleRoot, "src/main/kotlin/codes/spectrum/$packageDir/Start.kt")
        if(!main.exists()){
            main.writeText("""
package codes.spectrum.${packageDir.replace("/",".")}
fun main():Int{
    println("Empty application $packageDir")
    return -1
}
            """)
        }
    }
    
    val settingsFile = File(project.rootProject.rootDir, "settings.gradle.kts")
    var settingsContent = if (settingsFile.exists()) settingsFile.readText() else """
rootProject.name = "${project.rootProject.name}"
// include(/* add your modules here and uncomment */)
    """
    settingsContent = settingsContent.replace("""(//\s*)?include\(([^\)]*)\)""".toRegex()) {
        val modules = it.groups[2]!!.value.split(",")
            .map { it.replace("\"", "").trim() }
            .filter { !it.contains("*") && !it.isBlank() }
            .toMutableSet()
        modules.add(modulename)
        "include(${modules.map { "\"$it\"" }.joinToString()})"
    }
    settingsFile.writeText(settingsContent)
}
