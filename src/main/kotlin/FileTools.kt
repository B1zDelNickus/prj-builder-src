import org.gradle.api.Project
import java.io.File

fun Project.ensurePackage(name: String) {
    val packageDir = name.replace(".", "/")
    fun _ensurePackage(root: File) {
        root.mkdirs()
        if (root.listFiles().isEmpty()) {
            File(root, ".gitkeep").createNewFile()
        }
        var current = root.parentFile
        while (current.canonicalPath != this.projectDir.canonicalPath) {
            File(current, ".gitkeep").delete()
            current = current.parentFile
        }
    }
    _ensurePackage(File(this.projectDir, "src/main/kotlin/${packageDir}"))
    _ensurePackage(File(this.projectDir, "src/test/kotlin/${packageDir}"))

}

fun File.interpolate(map: Map<String, Any?>, pre: String = """~\{""", post: String = """\}""") {
    val text = this.readText()
    if (text.contains("~")) {
        var newcontent = text.replace("""$pre(.+?)$post""".toRegex()) {
            val key = it.groups[1]!!.value
            if (map.containsKey(key)) {
                map[key]?.toString() ?: ""
            } else {
                it.value
            }
        }
        if(newcontent!=text) {
            this.writeText(newcontent)
        }
    }
}