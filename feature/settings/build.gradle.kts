plugins {
    id("android.fruits.feature")
    id("android.fruits.test")
}
android {
    namespace = "com.fruits.settings"
}

dependencies {
    implementation(projects.core.navigation)
    implementation(projects.core.session)
}