plugins {
    id("android.fruits.feature")
    id("tech.fruits.koin")
    id("android.fruits.test")
}

android {
    namespace = "com.fruits.debug"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.data.network)
    implementation(projects.core.domain)
    implementation(projects.core.debug)
}