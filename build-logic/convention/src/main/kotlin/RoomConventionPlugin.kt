import androidx.room.gradle.RoomExtension
import hu.blu3berry.avalon.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class RoomConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target){
            with(pluginManager){
                apply("com.google.devtools.ksp")
                apply("androidx.room")
            }

            extensions.configure<RoomExtension>{
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                "commonMainApi"(libs.findLibrary("androidx-room-runtime").get())
                "commonMainApi"(libs.findLibrary("sqlite-bundled").get())
                "kspAndroid"(libs.findLibrary("androidx-room-compiler").get())
                "kspDesktop"(libs.findLibrary("androidx-room-compiler").get())
                "kspIosSimulatorArm64"(libs.findLibrary("androidx-room-compiler").get())
                "kspIosArm64"(libs.findLibrary("androidx-room-compiler").get())
                // kspIosX64 dropped — KMP project targets only iosArm64 + iosSimulatorArm64 (Apple Silicon dev env)
            }
        }
    }
}