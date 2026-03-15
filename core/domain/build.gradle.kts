plugins {
    id("android.fruits.library")
    id("tech.fruits.koin")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.domain"
}

dependencies {
    implementation(projects.core.logger)
}