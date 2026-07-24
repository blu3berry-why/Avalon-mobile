package hu.blu3berry.avalon.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>
) {
    with(commonExtension) {
        compileSdk = libs.findVersion("projectCompileSdkVersion").get().toString().toInt()

        defaultConfig.minSdk = libs.findVersion("projectMinSdkVersion").get().toString().toInt()

        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
            isCoreLibraryDesugaringEnabled = true
        }

        configureKotlin()

        dependencies {
            "coreLibraryDesugaring"(libs.findLibrary("android-desugarJdkLibs").get())
        }
    }
}

internal fun Project.configureKotlin() {
    val target = jvmTargetVersion
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(target)

            freeCompilerArgs.add(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            )
        }
    }
}