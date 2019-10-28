package sources

import interpolate
import org.gradle.api.Project
import java.io.File

data class SourceDefinition(
    var code: String = "",
    var name: String = "",
    var librarySet: Boolean = false,
    var className:String = ""
) {
    val classNamePrefix get() = if(className.isNullOrBlank()) code.replace("""(_|$|-|\.)(\w)""".toRegex()){
        it.groups[1]!!.value.toUpperCase()
    } else className
    val packageName get() = code.replace("-", "_").replace("/",".")
    fun packageName(proj: Project):String{
        return "codes.spectrum.sources.$packageName.${proj.name}"
    }
    fun interpolateMap(): Map<String, Any?> {
        return mapOf(
            "SOURCE_CODE" to code,
            "SOURCE_NAME" to name,
            "PACKAGE_NAME" to packageName,
            "CLASS_NAME" to classNamePrefix
        )
    }
    fun interpolate(file: File){
        file.interpolate(interpolateMap())
    }
}