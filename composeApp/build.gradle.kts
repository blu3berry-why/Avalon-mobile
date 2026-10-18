plugins {
    alias(libs.plugins.convention.cmp.library)
}

// :composeApp is a KMP library — shared UI + wiring consumed by :androidApp,
// :iosApp (framework), and the desktop JVM binary. Phase 0 skeleton: no feature
// modules wired yet.
kotlin {
    // The single iOS framework produced by this project (see CLAUDE.md — library modules
    // build klibs only). Linking runs on macOS; Linux hosts stop at klib compilation.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.domain)
                implementation(projects.core.data)

                implementation(libs.bundles.koin.compose.common)
                implementation(libs.kotlinx.serialization.json)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                implementation(libs.jetbrains.compose.navigation)
                implementation(libs.jetbrains.compose.viewmodel)
                implementation(libs.jetbrains.lifecycle.viewmodel)
                implementation(libs.jetbrains.lifecycle.compose)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "hu.blu3berry.avalon.MainKt"
        nativeDistributions {
            packageName = libs.versions.desktopPackageName.get()
            packageVersion = libs.versions.desktopPackageVersion.get()
        }
    }
}
