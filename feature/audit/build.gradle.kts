plugins {
    id("android.fruits.feature")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.audit"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.navigation)
}
