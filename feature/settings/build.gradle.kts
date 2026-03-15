plugins {
    id("android.fruits.feature")
    id("android.fruits.test")
}
android {
    namespace = "com.fruits.settings"
}
dependencies {
    implementation(projects.core.domain)
    implementation(libs.coil.compose)
}

