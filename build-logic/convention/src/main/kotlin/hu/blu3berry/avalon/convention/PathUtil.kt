package hu.blu3berry.avalon.convention

import org.gradle.api.Project
import java.util.Locale

fun Project.pathToPackageName(): String {
    val relativePackageName = path
        .replace(':', '_')
        .replace("-", "")
        .lowercase()

    return "hu.blu3berry.avalon$relativePackageName"
}

fun Project.pathToResourcePrefix(): String = path
    .replace(':', '_')
    .replace("-", "")
    .lowercase()
    .drop(1) + '_'

fun Project.pathToFrameworkName(): String {
    val parts = path.split(':', '-', '_', ' ')
    return parts.joinToString("") { part ->
        part.replaceFirstChar {
            it.titlecase(Locale.ROOT)
        }
    }
}
