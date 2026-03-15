plugins {
    id("android.fruits.application")
    id("android.fruits.compose")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.prod2026final"
    defaultConfig {
        applicationId = "com.fruits.prod2026final"
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    implementation(projects.core.navigation)
    implementation(projects.core.session)
    implementation(projects.core.logger)
    implementation(projects.core.data.repository)
    implementation(projects.core.data.network)
    implementation(projects.core.data.database)
    implementation(projects.feature.auth)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.profile)
    implementation(projects.feature.chat)
    implementation(projects.feature.chatList)
    implementation(projects.feature.tape)
    implementation(projects.core.domain)
    implementation(projects.core.debug)
    implementation(projects.feature.debugPanel)
}
