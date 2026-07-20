package hu.blu3berry.avalon.convention

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/** Bytecode level, from `projectJavaVersion` in the catalog — the single source of truth. */
private val Project.javaVersionString: String
    get() = libs.findVersion("projectJavaVersion").get().toString()

val Project.javaVersion: JavaVersion
    get() = JavaVersion.toVersion(javaVersionString)

val Project.jvmTargetVersion: JvmTarget
    get() = JvmTarget.fromTarget(javaVersionString)
