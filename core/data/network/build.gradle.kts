plugins {
    id("android.fruits.library")
    id("tech.fruits.ktor")
    id("tech.fruits.koin")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.network"

    buildFeatures {
        buildConfig = true
    }
}

dependencies{
    implementation(projects.core.domain)
}