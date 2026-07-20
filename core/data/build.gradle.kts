plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.kmpgen)
    alias(libs.plugins.ksp)
    alias(libs.plugins.blu3berry.kraft)
    alias(libs.plugins.convention.buildkonfig)
    // kotlin-serialization is applied by the kmp.library convention plugin.
}

// Layout A: one :core:data runs kmpgen on both client contracts. Two spec blocks keep the
// game API (avalon-spring) and the auth API (ForwardAuth) in separate generated packages,
// each producing its own kmpgen `Api` singleton (base URL set per-Api in DI).
kmpgen {
    spec(
        packageName = "hu.blu3berry.avalon.core.data.generated.game",
    ) {
        specFile = file("openapi/avalon-api.json")
    }
    spec(
        packageName = "hu.blu3berry.avalon.core.data.generated.auth",
    ) {
        specFile = file("openapi/forwardauth-api.yaml")
    }
}

// Kraft side aliases: `dto.toDomain()` / `domain.toDto()` on top of the convention plugin's
// verbose `to${target}From${source}` names. The dto pattern covers both generated packages
// (game + auth) — no DTO simple name is shared between them, so the aliases stay unambiguous.
ksp {
    arg("kraft.side.domain.name", "Domain")
    arg("kraft.side.domain.packagePattern", "hu.blu3berry.avalon.core.domain.model.**")
    arg("kraft.side.dto.name", "Dto")
    arg("kraft.side.dto.packagePattern", "hu.blu3berry.avalon.core.data.generated.**")
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/kmpgen")
            dependencies {
                implementation(projects.core.domain)

                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)

                implementation(libs.bundles.ktor.common)
                implementation(libs.bundles.koin.common)

                implementation(libs.multiplatform.settings)
                implementation(libs.touchlab.kermit)

                // kmpgen runtime + Arrow — needed by the Either<CallException, HttpCallResponse<D>>
                // -> Result bridge in network/EitherToResult.kt that every data source uses.
                implementation(libs.kmpgen.companion)
                implementation(libs.arrow.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
                implementation(libs.turbine)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.androidx.security.crypto)
                implementation(libs.koin.android)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}
