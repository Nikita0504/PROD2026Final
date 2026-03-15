plugins {
    id("android.fruits.feature")
    id("tech.fruits.koin")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.debug"
}

dependencies {
    implementation(projects.core.domain)
}