package hu.blu3berry.avalon.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<LibraryExtension>{
        namespace = this@configureKotlinMultiplatform.pathToPackageName()
    }

    configureAndroidTarget()

    extensions.configure<KotlinMultiplatformExtension> {
        // Desktop target — every library module exposes desktopMain so :composeApp can consume them on JVM
        jvm("desktop")

        // iosX64 (Intel Mac simulator) intentionally omitted — Nav3 1.1.0 + several other KMP libs
        // skip iosX64 publishing, and the dev environment is Apple Silicon. Aligns with KotlinIosTargets.kt
        // which uses the same Apple-Silicon-only set for the iOS framework.
        //
        // Targets only — no `binaries.framework` here. Library modules are consumed as klibs by
        // :composeApp, which is the single module that produces an actual framework (ComposeApp,
        // see KotlinIosTargets.kt). Declaring one per library built N unused .framework binaries
        // on every assemble and emitted the "Cannot infer a bundle ID ... use the bundle name
        // instead" warning for each.
        iosArm64()
        iosSimulatorArm64()

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        }
    }
}