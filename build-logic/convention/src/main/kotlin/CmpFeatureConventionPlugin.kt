import hu.blu3berry.avalon.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class CmpFeatureConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target){
            with(pluginManager){
                apply("hu.blu3berry.avalon.cmp.library")
            }

            dependencies {
                "commonMainImplementation"(project(":core:presentation"))
                "commonMainImplementation"(project(":core:designsystem:foundation"))
                "commonMainImplementation"(project(":core:designsystem:atoms"))
                "commonMainImplementation"(project(":core:designsystem:molecules"))

                "commonMainImplementation"(platform (libs.findLibrary("koin-bom").get()))
                "androidMainImplementation"(platform (libs.findLibrary("koin-bom").get()))

                "commonMainImplementation"(libs.findLibrary("koin-compose").get())
                "commonMainImplementation"(libs.findLibrary("koin-compose-viewmodel").get())

                "commonMainImplementation"(libs.findLibrary("jetbrains-compose-runtime").get())
                "commonMainImplementation"(libs.findLibrary("jetbrains-compose-viewmodel").get())
                "commonMainImplementation"(libs.findLibrary("jetbrains-lifecycle-viewmodel").get())
                "commonMainImplementation"(libs.findLibrary("jetbrains-lifecycle-compose").get())

                "commonMainImplementation"(libs.findLibrary("jetbrains-lifecycle-viewmodel-savedstate").get())
                "commonMainImplementation"(libs.findLibrary("jetbrains-savedstate").get())
                "commonMainImplementation"(libs.findLibrary("jetbrains-bundle").get())
                "commonMainImplementation"(libs.findLibrary("jetbrains-compose-navigation").get())


                "androidMainImplementation"(libs.findLibrary("koin-android").get())
                "androidMainImplementation"(libs.findLibrary("koin-androidx-compose").get())
                "androidMainImplementation"(libs.findLibrary("koin-androidx-navigation").get())
                "androidMainImplementation"(libs.findLibrary("koin-core-viewmodel").get())
            }
        }
    }
}