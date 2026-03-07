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
}

dependencies {
    implementation(libs.lifecycle.runtime.ktx)
    implementation(projects.core.navigation)
    implementation(projects.feature.auth)
    implementation(projects.feature.register)
}
