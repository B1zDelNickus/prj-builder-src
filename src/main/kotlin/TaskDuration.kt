import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.text.SimpleDateFormat
import java.util.*

val duration_format = SimpleDateFormat("mm:ss.sss")
fun Project.logTaskDuration() {
    allprojects {
        tasks.forEach {
            it.doFirst {
                it.extra.set("starttime", Date())
            }
            it.doLast {
                val start = it.extra.get("starttime") as Date
                val finish = Date()
                val duration = Date(finish.time - start.time)
                println("${this@logTaskDuration.name}:${it.name} duration: ${duration_format.format(duration)}")
            }
        }
    }
}