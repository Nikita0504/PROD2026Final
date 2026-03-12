plugins {
    id("android.fruits.library")
    id("tech.fruits.ktor")
    id("tech.fruits.koin")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.network"
}

dependencies{
    implementation(projects.core.domain)
}