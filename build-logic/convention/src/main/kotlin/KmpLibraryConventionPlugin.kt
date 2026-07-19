import com.android.build.api.dsl.LibraryExtension
import com.google.devtools.ksp.gradle.KspExtension
import hu.blu3berry.avalon.convention.configureKotlinAndroid
import hu.blu3berry.avalon.convention.configureKotlinMultiplatform
import hu.blu3berry.avalon.convention.libs
import hu.blu3berry.avalon.convention.pathToResourcePrefix
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target){
            with(pluginManager){
                apply("com.android.library")
                apply("com.google.devtools.ksp")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            configureKotlinMultiplatform()

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.named("commonMain") {
                    kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
                }
            }

            extensions.configure<LibraryExtension>{
                configureKotlinAndroid(this)

                resourcePrefix = this@with.pathToResourcePrefix()

                // required to make debug build of app run in iOS Simulator
                experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
            }

            // Configure kraft
            extensions.configure<KspExtension> {
                arg("kraft.functionNameFormat", "to${'$'}{target}From${'$'}{source}")
            }


            dependencies {
                "commonMainImplementation"(libs.findLibrary("kotlinx-serialization-json").get())
                "commonTestImplementation"(libs.findLibrary("kotlin-test").get())

                "commonMainImplementation"(libs.findLibrary("blu3berry-kraft-annotations").get())
                "kspCommonMainMetadata"(libs.findLibrary("blu3berry-kraft-ksp").get())
            }

            tasks.matching { it.name == "kspDebugKotlinAndroid" }.configureEach {
                dependsOn("kspCommonMainKotlinMetadata")
            }
            tasks.matching { it.name == "kspReleaseKotlinAndroid" }.configureEach {
                dependsOn("kspCommonMainKotlinMetadata")
            }
            // Wire commonMain KSP metadata generation as an input to all platform compile and
            // KSP tasks so the generated sources are visible during iOS / native / metadata
            // compilation and per-platform KSP rounds (e.g. Room compiler) see them too.
            tasks.matching { task ->
                val n = task.name
                (n.startsWith("compileKotlin") ||
                    n == "compileCommonMainKotlinMetadata" ||
                    n.startsWith("kspKotlin")) &&
                    n != "kspCommonMainKotlinMetadata"
            }.configureEach {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }
    }
}