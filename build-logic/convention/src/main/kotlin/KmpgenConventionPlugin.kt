import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpgenConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.kroegerama.openapi-kmp-gen")

        // kmpgen produces sources into build/generated/kmpgen, but doesn't auto-declare itself
        // as a dependency of the KSP / compile tasks that consume those files. Wire the chain
        // explicitly: kmpgenGenerateAll + kmpgenPrepare must run before any ksp* / compile*Kotlin*
        // task in the same project.
        afterEvaluate {
            tasks.matching { task ->
                val n = task.name
                n.startsWith("ksp") || (n.startsWith("compile") && n.contains("Kotlin"))
            }.configureEach {
                dependsOn("kmpgenGenerateAll")
                dependsOn("kmpgenPrepare")
            }
        }
    }
}
