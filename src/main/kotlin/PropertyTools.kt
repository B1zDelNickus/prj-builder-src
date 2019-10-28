import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.io.OutputStream

inline fun <reified T> Project.ensureProperty(name: String, defaultValue: T): T {
    if (null == this.findProperty(name)) {
        this.extra.set(name, defaultValue)
    }
    if (T::class == String::class) {
        return this.ensureProperty(name) as T
    }
    return this.findProperty(name)!! as T
}
inline fun <reified T> Project.ensureProperty(name: String): T {
    if (null == this.findProperty(name)) throw Exception("Cannot find property ${name}")
    if (T::class == String::class) {
        return this.ensureProperty(name) as T
    }
    return this.findProperty(name) as T
}

fun Project.ensureProperty(name: String): String {
    if (null == this.findProperty(name)) throw Exception("Cannot find property ${name}")
    return this.findProperty(name)!!.toString()
}
