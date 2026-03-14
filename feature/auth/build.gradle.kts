plugins {
    id("android.fruits.feature")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.auth"
}

dependencies {
    implementation(projects.core.navigation)
    implementation(projects.core.session)
    implementation(projects.core.domain)
}