plugins {
    alias(libs.plugins.convention.kmp.library)
}

// :core:domain — pure Kotlin domain: Result stack + session event bus. No platform deps.
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                // SharedFlow backs SessionManager's auth-event bus.
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
