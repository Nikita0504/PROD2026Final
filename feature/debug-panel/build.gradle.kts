plugins {
    id("android.fruits.feature")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.debugpanel"
}

dependencies {
    implementation(projects.core.navigation)
    implementation(projects.core.debug)
    implementation(projects.core.domain)
}