plugins {
    id("android.fruits.feature")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.profile"
}

dependencies {
    implementation(projects.core.session)
    implementation(libs.coil.compose)
    implementation(projects.core.domain)
    implementation(projects.core.navigation)
}