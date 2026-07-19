import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import hu.blu3berry.avalon.convention.pathToPackageName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class BuildKonfigConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.codingfeline.buildkonfig")
            }

            // Read the requested flavor from a Gradle property; default to "dev".
            // Run with: ./gradlew :composeApp:assembleDebug -Pflavor=staging
            val flavor = (target.findProperty("flavor") as? String) ?: "dev"

            extensions.configure<BuildKonfigExtension> {
                packageName.set(target.pathToPackageName())

                defaultConfigs {
                    buildConfigField(Type.STRING, "BASE_URL", devBaseUrl())
                }

                when (flavor) {
                    "staging" -> defaultConfigs("staging") {
                        buildConfigField(Type.STRING, "BASE_URL", stagingBaseUrl())
                    }
                    "dev" -> { /* default already applied */ }
                    else -> error(
                        "Unknown -Pflavor=$flavor. B1b ships only 'dev' and 'staging'. " +
                            "Production flavor lands in B1c-deploy."
                    )
                }
            }
        }
    }

    // Trailing slash is required: Ktor's DefaultRequest plugin merges the base into the
    // request URL via RFC-3986 relative-resolution, which treats a base without trailing
    // slash as a "file" and drops its last segment when the request URL has its own path.
    // Without the slash, `/api/v1` + appendPathSegments("auth","login") becomes
    // `/api/auth/login` — server returns 404 → ViewModel shows "something went wrong".
    private fun devBaseUrl(): String = "http://localhost:8080/api/v1/"

    // Staging URL is a placeholder pending B1c-deploy Coolify provisioning;
    // mirrors dev for now so the staging build still resolves a usable URL.
    private fun stagingBaseUrl(): String = "http://localhost:8080/api/v1/"
}
