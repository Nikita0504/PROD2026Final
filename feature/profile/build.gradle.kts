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
}