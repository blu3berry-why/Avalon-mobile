plugins {
    alias(libs.plugins.convention.android.application.compose)
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
}
