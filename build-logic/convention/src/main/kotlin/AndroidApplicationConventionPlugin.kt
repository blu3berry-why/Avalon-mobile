import com.android.build.api.dsl.ApplicationExtension
import hu.blu3berry.avalon.convention.configureKotlinAndroid
import hu.blu3berry.avalon.convention.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target){
            with(pluginManager){
                apply("com.android.application")
                // Kotlin Android plugin must be applied for .kt sources in src/main/kotlin
                // to compile. Previously :composeApp was the only consumer of this convention
                // (via cmp.application → kotlin.multiplatform, which provided Kotlin support).
                // After Step B's split, :androidApp uses this convention without KMP, so we
                // need the standalone Kotlin Android plugin here.
                // (AGP 9 will switch to built-in Kotlin via android.builtInKotlin=true; this
                // explicit plugin application becomes redundant there.)
                apply("org.jetbrains.kotlin.android")
            }
            extensions.configure<ApplicationExtension>{
                namespace = "hu.blu3berry.avalon"

                defaultConfig {
                    applicationId = libs.findVersion("projectApplicationId").get().toString()
                    targetSdk = libs.findVersion("projectTargetSdkVersion").get().toString().toInt()
                    versionCode = libs.findVersion("projectVersionCode").get().toString().toInt()
                    versionName = libs.findVersion("projectVersionName").get().toString()
                }
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                    }
                }

                configureKotlinAndroid(this)
            }
        }
    }

}