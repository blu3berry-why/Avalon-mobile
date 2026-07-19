import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "hu.blu3berry.avalon.convention.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.androidx.room.gradle.plugin)
    compileOnly("com.kroegerama.openapi-kmp-gen:gradle-plugin:${libs.versions.kmpgen.get()}")
    implementation(libs.buildkonfig.gradlePlugin)
    implementation(libs.buildkonfig.compiler)

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "hu.blu3berry.avalon.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("cmpLibrary") {
            id = "hu.blu3berry.avalon.cmp.library"
            implementationClass = "CmpLibraryConventionPlugin"
        }
        register("cmpFeature") {
            id = "hu.blu3berry.avalon.cmp.feature"
            implementationClass = "CmpFeatureConventionPlugin"
        }
        register("cmpApplication") {
            id = "hu.blu3berry.avalon.cmp.application"
            implementationClass = "CmpApplicationConventionPlugin"
        }
        register("androidApplication") {
            id = "hu.blu3berry.avalon.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "hu.blu3berry.avalon.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("room") {
            id = "hu.blu3berry.avalon.room"
            implementationClass = "RoomConventionPlugin"
        }
        register("buildkonfig") {
            id = "hu.blu3berry.avalon.buildkonfig"
            implementationClass = "BuildKonfigConventionPlugin"
        }
        register("kmpgen") {
            id = "hu.blu3berry.avalon.kmpgen"
            implementationClass = "KmpgenConventionPlugin"
        }
    }
}
