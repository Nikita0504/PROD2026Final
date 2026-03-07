plugins {
    id("android.fruits.feature")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.register"
}

dependencies {
    implementation(projects.core.navigation)
}

