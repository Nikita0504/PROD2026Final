plugins {
    id("android.fruits.feature")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.onboarding"
}

dependencies {
    implementation(projects.core.navigation)
    implementation(projects.core.session)
    implementation(projects.core.domain)
}

