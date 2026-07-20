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
                    buildConfigField(Type.STRING, "GAME_BASE_URL", devGameBaseUrl())
                    buildConfigField(Type.STRING, "AUTH_BASE_URL", devAuthBaseUrl())
                }

                when (flavor) {
                    "staging" -> defaultConfigs("staging") {
                        buildConfigField(Type.STRING, "GAME_BASE_URL", stagingGameBaseUrl())
                        buildConfigField(Type.STRING, "AUTH_BASE_URL", stagingAuthBaseUrl())
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

    // Two base URLs because Avalon runs two services: avalon-spring serves the game/lobby/user
    // endpoints, ForwardAuth serves login/register. Ports match docker-compose.yml in the
    // deploy repo (game 48080:8080, forwardauth 5000:5000).
    //
    // Trailing slash is required: Ktor's DefaultRequest plugin merges the base into the
    // request URL via RFC-3986 relative-resolution, which treats a base without trailing
    // slash as a "file" and drops its last segment when the request URL has its own path.
    // Avalon's endpoints hang off the root (no `/api/v1` prefix).
    private fun devGameBaseUrl(): String = "http://localhost:48080/"

    private fun devAuthBaseUrl(): String = "http://localhost:5000/"

    // Staging URLs are placeholders pending Coolify provisioning; they mirror dev so the
    // staging build still resolves a usable URL.
    private fun stagingGameBaseUrl(): String = "http://localhost:48080/"

    private fun stagingAuthBaseUrl(): String = "http://localhost:5000/"
}
