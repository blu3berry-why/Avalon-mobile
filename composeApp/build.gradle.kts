plugins {
    alias(libs.plugins.convention.cmp.library)
}

// :composeApp is a KMP library — shared UI + wiring consumed by :androidApp,
// :iosApp (framework), and the desktop JVM binary. Phase 0 skeleton: no feature
// modules wired yet.
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.bundles.koin.compose.common)
                implementation(libs.kotlinx.serialization.json)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
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
